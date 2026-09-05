package com.ruoyi.apiTool.domain.feed;

import java.math.BigDecimal;

/**
 * 当前二维码唯一匹配的黑湖物料库存明细。
 */
public class InventoryDetail {

    private Long inventoryElementId;
    private Long materialId;
    private Long storageLocationId;
    private Integer qcStatus;
    private String batchNo;
    private BigDecimal amount;
    private Long unitId;
    private String qrCode;

    public Long getInventoryElementId() {
        return inventoryElementId;
    }

    public void setInventoryElementId(Long inventoryElementId) {
        this.inventoryElementId = inventoryElementId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public Integer getQcStatus() {
        return qcStatus;
    }

    public void setQcStatus(Integer qcStatus) {
        this.qcStatus = qcStatus;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
