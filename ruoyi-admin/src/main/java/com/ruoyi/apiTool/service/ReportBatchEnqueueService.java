package com.ruoyi.apiTool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.domain.feed.ReportBatchResult;
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.mapper.InspectionReportMapper;
import com.ruoyi.inspection.service.IInspectionSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报工入队服务：将现有 reportBatch 的入队逻辑提取到独立事务 Service。
 * <p>
 * Controller 在入队事务成功提交后调用投料编排，保证投料失败不回滚报工记录。
 *
 * @author wmin
 * @date 2026-09-03
 */
@Service
public class ReportBatchEnqueueService {

    private static final Logger log = LoggerFactory.getLogger(ReportBatchEnqueueService.class);

    @Autowired
    private IInspectionSummaryService inspectionSummaryService;
    @Autowired
    private InspectionReportMapper inspectionReportMapper;

    /**
     * 执行原有报工入队逻辑：生成不良明细、更新主表状态。
     * 使用独立事务，确保入队成功后投料编排不会被回滚。
     *
     * @param params 前端传入的报工参数，包含 mainId
     * @return 报工入队结果，包含 summaryId 和主记录摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportBatchResult enqueue(Map<String, Object> params) {
        int mainId = (int) params.get("mainId");
        InspectionSummary inspectionSummary =
                inspectionSummaryService.selectInspectionSummaryByIdNoDetailAndBadItems((long) mainId);
        inspectionSummary.setSuccessFlag(0L);
        inspectionSummary.setApiDetail("待报工");

        // 解析不良项定义
        String allDefectItems = inspectionSummary.getAllDefectItems();
        if (allDefectItems == null || allDefectItems.trim().isEmpty()) {
            log.error("不良项数据为空，summaryId={}", inspectionSummary.getId());
            throw new RuntimeException("不良项数据为空，无法生成报工明细");
        }

        // 解析 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        List<CreateBadItemsTable> badItemList;
        try {
            badItemList = objectMapper.readValue(
                    allDefectItems,
                    new TypeReference<List<CreateBadItemsTable>>() {}
            );
        } catch (JsonProcessingException e) {
            log.error("解析 allDefectItems 失败，原始值：{}", allDefectItems, e);
            throw new RuntimeException("不良项数据解析失败");
        }

        // 生成不良明细
        List<InspectionReport> reportList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            BigDecimal qty = getDefectQty(inspectionSummary, i);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (i > badItemList.size()) {
                log.warn("defect_{} 数量为 {}，但未定义对应不良项", i, qty);
                continue;
            }
            CreateBadItemsTable badItem = badItemList.get(i - 1);
            InspectionReport report = new InspectionReport();
            report.setSummaryId(inspectionSummary.getId());
            report.setWorkOrderCode(inspectionSummary.getQrCode());
            report.setReportTime(inspectionSummary.getUpdateTime());
            report.setQuantity(qty);
            report.setSuccessFlag(0);
            report.setDefectRemark(badItem.getBadName());
            reportList.add(report);
        }
        if (!reportList.isEmpty()) {
            inspectionReportMapper.batchInsert(reportList);
        }

        // 更新主表
        int updateFlag = inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
        if (updateFlag <= 0) {
            throw new RuntimeException("更新汇总表失败");
        }

        inspectionSummary.setInspectionReportList(null);
        return new ReportBatchResult(inspectionSummary.getId(), inspectionSummary);
    }

    /**
     * 通过反射读取主表的不良项数量
     */
    private BigDecimal getDefectQty(InspectionSummary summary, int index) {
        try {
            String fieldName = "defect" + index;
            Field field = InspectionSummary.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(summary);
            return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
        } catch (Exception e) {
            throw new RuntimeException("读取 defect" + index + " 失败", e);
        }
    }
}
