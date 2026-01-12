package com.ruoyi.inspection.domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 镜检统计主对象 inspection_summary
 *
 * @author w
 * @date 2025-07-08
 */
public class InspectionSummary extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 工单号
     */
    @Excel(name = "工单号")
    private String workOrderCode;

    /**
     * 总数量
     */
    @Excel(name = "总数量")
    private int totalQuantity;

    /**
     * 不合格总数
     */
    @Excel(name = "不合格总数")
    private int defectiveTotal;

    /**
     * 不良1
     */
    @Excel(name = "不良1")
    private int defect1;

    /**
     * 不良2
     */
    @Excel(name = "不良2")
    private int defect2;

    /**
     * 不良3
     */
    @Excel(name = "不良3")
    private int defect3;

    /**
     * 不良4
     */
    @Excel(name = "不良4")
    private int defect4;

    /**
     * 不良5
     */
    @Excel(name = "不良5")
    private int defect5;

    /**
     * 不良6
     */
    @Excel(name = "不良6")
    private int defect6;

    /**
     * 不良7
     */
    @Excel(name = "不良7")
    private int defect7;

    /**
     * 不良8
     */
    @Excel(name = "不良8")
    private int defect8;

    /**
     * 不良9
     */
    @Excel(name = "不良9")
    private int defect9;

    /**
     * 不良10
     */
    @Excel(name = "不良10")
    private int defect10;

    /**
     * 不良11
     */
    @Excel(name = "不良11")
    private int defect11;

    /**
     * 不良12
     */
    @Excel(name = "不良12")
    private int defect12;

    /**
     * 不良13
     */
    @Excel(name = "不良13")
    private int defect13;

    /**
     * 不良14
     */
    @Excel(name = "不良14")
    private int defect14;

    /**
     * 不良15
     */
    @Excel(name = "不良15")
    private int defect15;

    /**
     * 不良16
     */
    @Excel(name = "不良16")
    private int defect16;

    /**
     * 不良17
     */
    @Excel(name = "不良17")
    private int defect17;

    /**
     * 不良18
     */
    @Excel(name = "不良18")
    private int defect18;

    /**
     * 不良19
     */
    @Excel(name = "不良19")
    private int defect19;

    /**
     * 不良20
     */
    @Excel(name = "不良20")
    private int defect20;
    /**
     * 不良20
     */
    @Excel(name = "全部缺陷项")
    private String allDefectItems;
    /**
     * 不良20
     */
    @Excel(name = "暂停时间")
    private String stopTime;
    /**
     * 不良20
     */
    @Excel(name = "api反馈")
    private String apiDetail;
    @Excel(name = "二维码")
    private String qrCode;


    @Excel(name = "度数")
    private String degrees;
    @Excel(name = "预报api")
    private String apiReport;
    private String color;
    /**
     * 成功状态，true表示成功
     */
    @Excel(name = "成功状态，1表示成功")
    private Long successFlag;
    private String debounce;
    private String previousProcessSetting;

    /**
     * 报工记录信息
     */
    private List<InspectionReport> inspectionReportList;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setDefectiveTotal(int defectiveTotal) {
        this.defectiveTotal = defectiveTotal;
    }

    public int getDefectiveTotal() {
        return defectiveTotal;
    }



    public String getAllDefectItems() {
        return allDefectItems;
    }

    public void setAllDefectItems(String allDefectItems) {
        this.allDefectItems = allDefectItems;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getApiDetail() {
        return apiDetail;
    }

    public void setApiDetail(String apiDetail) {
        this.apiDetail = apiDetail;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<InspectionReport> getInspectionReportList() {
        return inspectionReportList;
    }

    public void setInspectionReportList(List<InspectionReport> inspectionReportList) {
        this.inspectionReportList = inspectionReportList;
    }

    public String getDegrees() {
        return degrees;
    }

    public void setDegrees(String degrees) {
        this.degrees = degrees;
    }



    public String getApiReport() {
        return apiReport;
    }

    public void setApiReport(String apiReport) {
        this.apiReport = apiReport;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public String getDebounce() {
        return debounce;
    }

    public void setDebounce(String debounce) {
        this.debounce = debounce;
    }

    public String getPreviousProcessSetting() {
        return previousProcessSetting;
    }

    public void setPreviousProcessSetting(String previousProcessSetting) {
        this.previousProcessSetting = previousProcessSetting;
    }

    public Long getSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(Long successFlag) {
        this.successFlag = successFlag;
    }

    public int getDefect1() {
        return defect1;
    }

    public void setDefect1(int defect1) {
        this.defect1 = defect1;
    }

    public int getDefect2() {
        return defect2;
    }

    public void setDefect2(int defect2) {
        this.defect2 = defect2;
    }

    public int getDefect3() {
        return defect3;
    }

    public void setDefect3(int defect3) {
        this.defect3 = defect3;
    }

    public int getDefect4() {
        return defect4;
    }

    public void setDefect4(int defect4) {
        this.defect4 = defect4;
    }

    public int getDefect5() {
        return defect5;
    }

    public void setDefect5(int defect5) {
        this.defect5 = defect5;
    }

    public int getDefect6() {
        return defect6;
    }

    public void setDefect6(int defect6) {
        this.defect6 = defect6;
    }

    public int getDefect7() {
        return defect7;
    }

    public void setDefect7(int defect7) {
        this.defect7 = defect7;
    }

    public int getDefect8() {
        return defect8;
    }

    public void setDefect8(int defect8) {
        this.defect8 = defect8;
    }

    public int getDefect9() {
        return defect9;
    }

    public void setDefect9(int defect9) {
        this.defect9 = defect9;
    }

    public int getDefect10() {
        return defect10;
    }

    public void setDefect10(int defect10) {
        this.defect10 = defect10;
    }

    public int getDefect11() {
        return defect11;
    }

    public void setDefect11(int defect11) {
        this.defect11 = defect11;
    }

    public int getDefect12() {
        return defect12;
    }

    public void setDefect12(int defect12) {
        this.defect12 = defect12;
    }

    public int getDefect13() {
        return defect13;
    }

    public void setDefect13(int defect13) {
        this.defect13 = defect13;
    }

    public int getDefect14() {
        return defect14;
    }

    public void setDefect14(int defect14) {
        this.defect14 = defect14;
    }

    public int getDefect15() {
        return defect15;
    }

    public void setDefect15(int defect15) {
        this.defect15 = defect15;
    }

    public int getDefect16() {
        return defect16;
    }

    public void setDefect16(int defect16) {
        this.defect16 = defect16;
    }

    public int getDefect17() {
        return defect17;
    }

    public void setDefect17(int defect17) {
        this.defect17 = defect17;
    }

    public int getDefect18() {
        return defect18;
    }

    public void setDefect18(int defect18) {
        this.defect18 = defect18;
    }

    public int getDefect19() {
        return defect19;
    }

    public void setDefect19(int defect19) {
        this.defect19 = defect19;
    }

    public int getDefect20() {
        return defect20;
    }

    public void setDefect20(int defect20) {
        this.defect20 = defect20;
    }

    @Override
    public String toString() {
        return "InspectionSummary{" +
                "id=" + id +
                ", workOrderCode='" + workOrderCode + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", defectiveTotal=" + defectiveTotal +
                ", defect1=" + defect1 +
                ", defect2=" + defect2 +
                ", defect3=" + defect3 +
                ", defect4=" + defect4 +
                ", defect5=" + defect5 +
                ", defect6=" + defect6 +
                ", defect7=" + defect7 +
                ", defect8=" + defect8 +
                ", defect9=" + defect9 +
                ", defect10=" + defect10 +
                ", defect11=" + defect11 +
                ", defect12=" + defect12 +
                ", defect13=" + defect13 +
                ", defect14=" + defect14 +
                ", defect15=" + defect15 +
                ", defect16=" + defect16 +
                ", defect17=" + defect17 +
                ", defect18=" + defect18 +
                ", defect19=" + defect19 +
                ", defect20=" + defect20 +
                ", allDefectItems='" + allDefectItems + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", apiDetail='" + apiDetail + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", degrees='" + degrees + '\'' +
                ", apiReport='" + apiReport + '\'' +
                ", color='" + color + '\'' +
                ", successFlag=" + successFlag +
                ", debounce='" + debounce + '\'' +
                ", previousProcessSetting='" + previousProcessSetting + '\'' +
                ", inspectionReportList=" + inspectionReportList +
                '}';
    }
}
