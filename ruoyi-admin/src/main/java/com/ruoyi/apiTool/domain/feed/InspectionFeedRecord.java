package com.ruoyi.apiTool.domain.feed;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 镜检投料上传记录对象 inspection_feed_record
 *
 * @author wmin
 * @date 2026-09-03
 */
public class InspectionFeedRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 投料记录ID */
    private Long id;

    /** 镜检主记录ID */
    private Long summaryId;

    /** 扫码二维码 */
    private String qrCode;

    /** 工单号 */
    private String workOrderCode;

    /** 黑湖生产任务ID */
    private Long taskId;

    /** 库存原料物料ID */
    private Long materialId;

    /** 库存明细ID */
    private Long inventoryElementId;

    /** 投料数量 */
    private BigDecimal feedAmount;

    /** 投料单位ID */
    private Long unitId;

    /** 最近一次批量投料请求JSON */
    private String requestJson;

    /** 最近一次黑湖响应JSON */
    private String responseJson;

    /** 上传状态：PENDING/PROCESSING/SUCCESS/FAILED/UNKNOWN */
    private String uploadStatus;

    /** 失败原因 */
    private String errorMessage;

    /** 失败类型：FEED_RELATION=获取投料关系无效，INVENTORY=库存明细查询无效，UPLOAD=上传失败 */
    private String failType;

    /** 手动重传次数 */
    private Integer retryCount;

    /** 上传成功时间 */
    private Date feedTime;

    /** 投料记录ID */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 镜检主记录ID */
    public Long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Long summaryId) {
        this.summaryId = summaryId;
    }

    /** 扫码二维码 */
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    /** 工单号 */
    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    /** 黑湖生产任务ID */
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /** 库存原料物料ID */
    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    /** 库存明细ID */
    public Long getInventoryElementId() {
        return inventoryElementId;
    }

    public void setInventoryElementId(Long inventoryElementId) {
        this.inventoryElementId = inventoryElementId;
    }

    /** 投料数量 */
    public BigDecimal getFeedAmount() {
        return feedAmount;
    }

    public void setFeedAmount(BigDecimal feedAmount) {
        this.feedAmount = feedAmount;
    }

    /** 投料单位ID */
    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    /** 最近一次批量投料请求JSON */
    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    /** 最近一次黑湖响应JSON */
    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    /** 上传状态：PENDING/PROCESSING/SUCCESS/FAILED/UNKNOWN */
    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    /** 失败原因 */
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** 失败类型：FEED_RELATION/INVENTORY/UPLOAD */
    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType;
    }

    /** 手动重传次数 */
    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /** 上传成功时间 */
    public Date getFeedTime() {
        return feedTime;
    }

    public void setFeedTime(Date feedTime) {
        this.feedTime = feedTime;
    }
}
