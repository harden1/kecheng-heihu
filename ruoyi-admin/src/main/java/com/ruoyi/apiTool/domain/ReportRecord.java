package com.ruoyi.apiTool.domain;


import java.util.Map;

public class ReportRecord {
    private String batchNo;
    private String amount;
    private String batchNoId;
    private String qrCode;
    private String processId;
    private String name;
    private String specification;
    private String materialCode;
    private String workOrderId;
    private String materialId;
    private String taskId;
    private Map<String, String> allDefectItems;
    private String creatBy;
    private String flag;
    private String mesUserName;
    // ✅ 添加这个字段
    private String mesUserId;

    // ✅ 建议加上 getter/setter
    public String getMesUserId() {
        return mesUserId;
    }

    public void setMesUserId(String mesUserId) {
        this.mesUserId = mesUserId;
    }

    public String getCreatBy() {
        return creatBy;
    }

    public void setCreatBy(String creatBy) {
        this.creatBy = creatBy;
    }
    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBatchNoId() {
        return batchNoId;
    }

    public void setBatchNoId(String batchNoId) {
        this.batchNoId = batchNoId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Map<String, String> getAllDefectItems() {
        return allDefectItems;
    }

    public void setAllDefectItems(Map<String, String> allDefectItems) {
        this.allDefectItems = allDefectItems;
    }

    @Override
    public String toString() {
        return "ReportRecord{" +
                "batchNo='" + batchNo + '\'' +
                ", amount='" + amount + '\'' +
                ", batchNoId='" + batchNoId + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", processId='" + processId + '\'' +
                ", name='" + name + '\'' +
                ", specification='" + specification + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", workOrderId='" + workOrderId + '\'' +
                ", materialId='" + materialId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", allDefectItems=" + allDefectItems +
                ", creatBy='" + creatBy + '\'' +
                ", flag='" + flag + '\'' +
                ", mesUserName='" + mesUserName + '\'' +
                ", mesUserId='" + mesUserId + '\'' +
                '}';
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getMesUserName() {
        return mesUserName;
    }

    public void setMesUserName(String mesUserName) {
        this.mesUserName = mesUserName;
    }
}