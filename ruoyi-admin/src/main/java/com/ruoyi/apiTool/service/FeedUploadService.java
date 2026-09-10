package com.ruoyi.apiTool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.client.BulkFeedClient;
import com.ruoyi.apiTool.client.FeedRelationClient;
import com.ruoyi.apiTool.client.MaterialInventoryClient;
import com.ruoyi.apiTool.domain.feed.BlackLakeResult;
import com.ruoyi.apiTool.domain.feed.BulkFeedRequest;
import com.ruoyi.apiTool.domain.feed.InspectionFeedRecord;
import com.ruoyi.apiTool.domain.feed.InventoryDetail;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UncheckedIOException;
import java.util.Date;
import java.util.Map;

/**
 * 投料编排服务：自动投料和手动重传的核心编排逻辑。
 * <p>
 * 固定调用顺序：创建待上传投料记录 → 投料关系查询 → 库存查询 → 批量投料 → 更新投料记录状态。
 * 自动投料失败不阻断现有 reportBatch 入队和定时 _progress_report。
 *
 * @author wmin
 * @date 2026-09-03
 */
@Service
public class FeedUploadService {

    private static final Logger log = LoggerFactory.getLogger(FeedUploadService.class);

    @Autowired
    private MaterialInventoryClient materialInventoryClient;
    @Autowired
    private FeedRelationClient feedRelationClient;
    @Autowired
    private BulkFeedClient bulkFeedClient;
    @Autowired
    private IInspectionFeedRecordService feedRecordService;
    @Autowired
    private IInspectionSummaryService inspectionSummaryService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 自动投料入口：点击报工时触发。
     * 按 summaryId 查找镜检主记录，解析 api_report 获取 qrCode 和 taskId，
     * 创建或读取唯一投料记录，然后执行投料上传。
     *
     * @param summaryId 镜检主记录ID
     * @return 投料记录最终状态
     */
    public InspectionFeedRecord autoUpload(Long summaryId) {
        InspectionSummary summary = inspectionSummaryService.selectInspectionSummaryByIdNoDetailAndBadItems(summaryId);
        if (summary == null) {
            throw new ServiceException("镜检主记录不存在，summaryId=" + summaryId);
        }

        // 解析 api_report 获取 qrCode、taskId 和 materialId
        Map<String, Object> reportInfo = parseApiReport(summary.getApiReport());
        String qrCode = requiredText(reportInfo, "qrCode");
        Long taskId = requiredLong(reportInfo, "taskId");
        Long materialId = requiredLong(reportInfo, "materialId");

        // 创建或读取唯一投料记录
        InspectionFeedRecord record = feedRecordService.getOrCreate(summary, qrCode, taskId, materialId);

        // 已成功的记录不再重复上传
        if ("SUCCESS".equals(record.getUploadStatus())) {
            return record;
        }

        return upload(record, false);
    }

    /**
     * 手动重传入口：投料记录页面点击"重新上传"。
     * 仅 PENDING/FAILED 状态可重传，重新查询库存和投料关系。
     *
     * @param recordId 投料记录ID
     * @return 投料记录最终状态
     */
    public InspectionFeedRecord manualRetry(Long recordId) {
        InspectionFeedRecord record = feedRecordService.selectById(recordId);
        if (record == null) {
            throw new ServiceException("投料记录不存在");
        }

        // SUCCESS、PROCESSING、UNKNOWN 均禁止手动重传
        String status = record.getUploadStatus();
        if ("SUCCESS".equals(status)) {
            throw new ServiceException("已成功的投料记录不允许重传");
        }
        if ("PROCESSING".equals(status)) {
            throw new ServiceException("正在上传中的投料记录不允许重传");
        }
        if ("UNKNOWN".equals(status)) {
            throw new ServiceException("结果未知的投料记录需人工核对后才能重传");
        }

        return upload(record, true);
    }

    /**
     * 统一上传方法：抢占 → 投料关系查询 → 库存查询 → 批量投料 → 更新状态。
     *
     * @param record 投料记录
     * @param manual true=手动重传，false=自动上传
     * @return 投料记录最终状态
     */
    private InspectionFeedRecord upload(InspectionFeedRecord record, boolean manual) {
        // 抢占状态
        boolean claimed = manual
                ? feedRecordService.claimManualRetry(record.getId())
                : feedRecordService.claimAutoUpload(record.getId());
        if (!claimed) {
            // 抢占失败，返回当前状态
            return feedRecordService.selectById(record.getId());
        }

        String requestJson = null;
        String responseJson = null;
        try {
            // 1. 固定投料关系查询物料ID
            FeedRelationClient.FeedRelationResult feedRelation = feedRelationClient.getFeedRelation(1787200480054357L, record.getTaskId());

            // 2. 库存查询（用二维码 + 投料关系返回的物料编号查库存明细）
            InventoryDetail inventory = materialInventoryClient.queryByQrCode(record.getQrCode(), feedRelation.getMaterialCode());

            // 3. 组装批量投料请求
            BulkFeedRequest request = BulkFeedRequest.build(record.getTaskId(), record.getQrCode(), inventory, feedRelation.getFeedKey());
            requestJson = bulkFeedClient.serializeRequest(request);

            // 4. 调用批量投料写接口
            BlackLakeResult result = bulkFeedClient.bulkFeed(request);
            responseJson = serializeResponse(result);

            if (result.isSuccess()) {
                // code=200：投料成功
                feedRecordService.markSuccess(record.getId(), requestJson, responseJson, new Date());
            } else {
                // 明确业务失败（上传接口返回非200）
                String errorMsg = "黑湖投料业务失败，code=" + result.getCode() + "，message=" + result.getMessage();
                feedRecordService.markFailed(record.getId(), requestJson, responseJson, errorMsg, "UPLOAD");
            }
        } catch (UncheckedIOException e) {
            // 网络异常：连接失败表示请求未发送，标记为 FAILED
            // 读取超时表示请求可能已到达黑湖，标记为 UNKNOWN
            String errorMsg = "调用黑湖接口发生网络异常: " + e.getMessage();
            log.error("投料网络异常，recordId={}", record.getId(), e);
            if (isReadTimeout(e)) {
                feedRecordService.markUnknown(record.getId(), requestJson, responseJson, errorMsg, "UPLOAD");
            } else {
                feedRecordService.markFailed(record.getId(), requestJson, responseJson, errorMsg, "UPLOAD");
            }
        } catch (IllegalStateException e) {
            // 明确的业务异常，根据异常来源区分失败类型
            String errorMsg = e.getMessage();
            log.error("投料业务异常，recordId={}", record.getId(), e);
            String failType = resolveFailType(e.getMessage());
            feedRecordService.markFailed(record.getId(), requestJson, responseJson, errorMsg, failType);
        } catch (Exception e) {
            // 未预期异常，标记为 FAILED
            String errorMsg = "投料未预期异常: " + e.getMessage();
            log.error("投料未预期异常，recordId={}", record.getId(), e);
            feedRecordService.markFailed(record.getId(), requestJson, responseJson, errorMsg, "UPLOAD");
        }

        return feedRecordService.selectById(record.getId());
    }

    /**
     * 解析 api_report JSON 字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseApiReport(String apiReport) {
        if (apiReport == null || apiReport.trim().isEmpty()) {
            throw new ServiceException("镜检主记录 api_report 为空");
        }
        try {
            return objectMapper.readValue(apiReport, Map.class);
        } catch (Exception e) {
            throw new ServiceException("解析 api_report 失败: " + e.getMessage());
        }
    }

    /**
     * 从 Map 中获取必填文本字段
     */
    private String requiredText(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new ServiceException("api_report 缺少必填字段: " + key);
        }
        return value.toString();
    }

    /**
     * 从 Map 中获取必填 Long 字段
     */
    private Long requiredLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new ServiceException("api_report 缺少必填字段: " + key);
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new ServiceException("api_report 字段 " + key + " 不是有效数字: " + value);
        }
    }

    /**
     * 序列化黑湖响应为 JSON 字符串
     */
    private String serializeResponse(BlackLakeResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("序列化黑湖响应失败", e);
            return null;
        }
    }

    /**
     * 判断异常是否为读取超时
     */
    private boolean isReadTimeout(UncheckedIOException e) {
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null) {
            String msg = cause.getMessage().toLowerCase();
            return msg.contains("timeout") || msg.contains("timed out");
        }
        return false;
    }

    /**
     * 根据异常消息区分失败类型。
     * FeedRelationClient 抛出的异常包含"投料关系"，MaterialInventoryClient 抛出的异常包含"库存"。
     *
     * @param message 异常消息
     * @return FEED_RELATION=获取投料关系无效，INVENTORY=库存明细查询无效，UPLOAD=上传失败
     */
    private String resolveFailType(String message) {
        if (message == null) {
            return "UPLOAD";
        }
        if (message.contains("投料关系")) {
            return "FEED_RELATION";
        }
        if (message.contains("库存")) {
            return "INVENTORY";
        }
        return "UPLOAD";
    }
}
