package com.ruoyi.apiTool.domain.feed;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 黑湖批量投料请求体。
 * <p>
 * 字段来源：
 * <ul>
 *   <li>feedKey：第三个投料关系接口返回的完整 alternativeFeedKey</li>
 *   <li>inventoryRange：第二个库存接口响应构造</li>
 *   <li>opeAmountDetail：第二个库存接口 amount.amount 和 amount.unit.id</li>
 *   <li>taskId：当前镜检主记录 api_report.taskId</li>
 * </ul>
 *
 * @author wmin
 * @date 2026-09-03
 */
public class BulkFeedRequest {

    /** 投料明细列表，当前需求一次只处理一条 */
    private List<FeedItem> feedItems;

    /** 跳过弱控制规则，固定为 SKIP_LOT_CONTROL 和 SKIP_EXPIRE_CHECK */
    private List<String> skipWeakControlRule;

    /** 黑湖生产任务ID */
    private Long taskId;

    /**
     * 构造批量投料请求。
     *
     * @param taskId             生产任务ID
     * @param inventoryIdentifier 当前工单扫码二维码
     * @param inventory           库存明细
     * @param feedKey             投料关系接口返回的完整 alternativeFeedKey
     * @return 组装完成的批量投料请求
     */
    public static BulkFeedRequest build(Long taskId, String inventoryIdentifier,
                                        InventoryDetail inventory, JsonNode feedKey) {
        BulkFeedRequest request = new BulkFeedRequest();
        request.taskId = taskId;
        request.skipWeakControlRule = Arrays.asList("SKIP_LOT_CONTROL", "SKIP_EXPIRE_CHECK");

        FeedItem item = new FeedItem();
        item.feedKey = feedKey;

        // 构造库存范围
        InventoryRange range = new InventoryRange();
        range.locationIds = Collections.singletonList(inventory.getStorageLocationId());
        range.qcStatuses = Collections.singletonList(inventory.getQcStatus());
        // 批号非空时才传
        if (inventory.getBatchNo() != null && !inventory.getBatchNo().isEmpty()) {
            range.batchNos = Collections.singletonList(inventory.getBatchNo());
        }
        range.inventoryElementId = inventory.getInventoryElementId();
        range.inventoryIdentifier = inventoryIdentifier;
        item.inventoryRange = range;

        // 构造投料数量明细
        OpeAmountDetail detail = new OpeAmountDetail();
        detail.opeAmount = inventory.getAmount();
        detail.opeUnitId = inventory.getUnitId();
        item.opeAmountDetail = detail;

        // 固定备注
        item.remark = "镜检投料";

        request.feedItems = Collections.singletonList(item);
        return request;
    }

    public List<FeedItem> getFeedItems() {
        return feedItems;
    }

    public void setFeedItems(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
    }

    public List<String> getSkipWeakControlRule() {
        return skipWeakControlRule;
    }

    public void setSkipWeakControlRule(List<String> skipWeakControlRule) {
        this.skipWeakControlRule = skipWeakControlRule;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 投料明细项
     */
    public static class FeedItem {
        /** 投料关系键，完整保留第三个接口返回的 alternativeFeedKey */
        private JsonNode feedKey;
        /** 库存范围 */
        private InventoryRange inventoryRange;
        /** 投料数量明细 */
        private OpeAmountDetail opeAmountDetail;
        /** 备注，固定"镜检投料" */
        private String remark;

        public JsonNode getFeedKey() {
            return feedKey;
        }

        public void setFeedKey(JsonNode feedKey) {
            this.feedKey = feedKey;
        }

        public InventoryRange getInventoryRange() {
            return inventoryRange;
        }

        public void setInventoryRange(InventoryRange inventoryRange) {
            this.inventoryRange = inventoryRange;
        }

        public OpeAmountDetail getOpeAmountDetail() {
            return opeAmountDetail;
        }

        public void setOpeAmountDetail(OpeAmountDetail opeAmountDetail) {
            this.opeAmountDetail = opeAmountDetail;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 库存范围
     */
    public static class InventoryRange {
        /** 仓位ID列表 */
        private List<Long> locationIds;
        /** 质量状态列表 */
        private List<Integer> qcStatuses;
        /** 批号列表，非空时传 */
        private List<String> batchNos;
        /** 库存明细ID */
        private Long inventoryElementId;
        /** 库存标识，当前工单扫码二维码 */
        private String inventoryIdentifier;

        public List<Long> getLocationIds() {
            return locationIds;
        }

        public void setLocationIds(List<Long> locationIds) {
            this.locationIds = locationIds;
        }

        public List<Integer> getQcStatuses() {
            return qcStatuses;
        }

        public void setQcStatuses(List<Integer> qcStatuses) {
            this.qcStatuses = qcStatuses;
        }

        public List<String> getBatchNos() {
            return batchNos;
        }

        public void setBatchNos(List<String> batchNos) {
            this.batchNos = batchNos;
        }

        public Long getInventoryElementId() {
            return inventoryElementId;
        }

        public void setInventoryElementId(Long inventoryElementId) {
            this.inventoryElementId = inventoryElementId;
        }

        public String getInventoryIdentifier() {
            return inventoryIdentifier;
        }

        public void setInventoryIdentifier(String inventoryIdentifier) {
            this.inventoryIdentifier = inventoryIdentifier;
        }
    }

    /**
     * 投料数量明细
     */
    public static class OpeAmountDetail {
        /** 投料数量，取自库存接口 amount.amount */
        private BigDecimal opeAmount;
        /** 投料单位ID，取自库存接口 amount.unit.id */
        private Long opeUnitId;

        public BigDecimal getOpeAmount() {
            return opeAmount;
        }

        public void setOpeAmount(BigDecimal opeAmount) {
            this.opeAmount = opeAmount;
        }

        public Long getOpeUnitId() {
            return opeUnitId;
        }

        public void setOpeUnitId(Long opeUnitId) {
            this.opeUnitId = opeUnitId;
        }
    }
}
