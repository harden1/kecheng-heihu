package com.ruoyi.apiTool.service;

import com.ruoyi.apiTool.domain.feed.InspectionFeedRecord;
import com.ruoyi.inspection.domain.InspectionSummary;

import java.util.Date;
import java.util.List;

/**
 * 镜检投料上传记录Service接口
 *
 * @author wmin
 * @date 2026-09-03
 */
public interface IInspectionFeedRecordService {

    /**
     * 根据主键查询投料记录
     *
     * @param id 投料记录ID
     * @return 投料记录
     */
    InspectionFeedRecord selectById(Long id);

    /**
     * 按条件查询投料记录列表
     *
     * @param query 查询条件
     * @return 投料记录列表
     */
    List<InspectionFeedRecord> selectList(InspectionFeedRecord query);

    /**
     * 按业务键获取或创建唯一投料记录。
     * 同一 summaryId + taskId + qrCode 只创建一条记录，并发插入遇到唯一键冲突时重新查询。
     *
     * @param summary    镜检主记录
     * @param qrCode     扫码二维码
     * @param taskId     黑湖生产任务ID
     * @param materialId 库存原料物料ID（来自 api_report）
     * @return 投料记录，初始状态为 PENDING
     */
    InspectionFeedRecord getOrCreate(InspectionSummary summary, String qrCode, Long taskId, Long materialId);

    /**
     * 自动上传抢占：仅 PENDING 状态可抢占为 PROCESSING
     *
     * @param id 投料记录ID
     * @return true 表示抢占成功
     */
    boolean claimAutoUpload(Long id);

    /**
     * 手动重传抢占：仅 PENDING/FAILED 状态可抢占为 PROCESSING
     *
     * @param id 投料记录ID
     * @return true 表示抢占成功
     */
    boolean claimManualRetry(Long id);

    /**
     * 标记投料成功
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param feedTime     上传成功时间
     * @return 影响行数
     */
    int markSuccess(Long id, String requestJson, String responseJson, Date feedTime);

    /**
     * 标记投料失败
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param errorMessage 失败原因
     * @return 影响行数
     */
    int markFailed(Long id, String requestJson, String responseJson, String errorMessage, String failType);

    /**
     * 标记投料结果未知
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int markUnknown(Long id, String requestJson, String responseJson, String errorMessage, String failType);
}
