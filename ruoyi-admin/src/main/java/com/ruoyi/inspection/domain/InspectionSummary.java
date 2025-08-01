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
public class InspectionSummary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 工单号 */
    @Excel(name = "工单号")
    private String workOrderCode;

    /** 总数量 */
    @Excel(name = "总数量")
    private Long totalQuantity;

    /** 不合格总数 */
    @Excel(name = "不合格总数")
    private Long defectiveTotal;

    /** 不良1 */
    @Excel(name = "不良1")
    private Long defect1;

    /** 不良2 */
    @Excel(name = "不良2")
    private Long defect2;

    /** 不良3 */
    @Excel(name = "不良3")
    private Long defect3;

    /** 不良4 */
    @Excel(name = "不良4")
    private Long defect4;

    /** 不良5 */
    @Excel(name = "不良5")
    private Long defect5;

    /** 不良6 */
    @Excel(name = "不良6")
    private Long defect6;

    /** 不良7 */
    @Excel(name = "不良7")
    private Long defect7;

    /** 不良8 */
    @Excel(name = "不良8")
    private Long defect8;

    /** 不良9 */
    @Excel(name = "不良9")
    private Long defect9;

    /** 不良10 */
    @Excel(name = "不良10")
    private Long defect10;

    /** 不良11 */
    @Excel(name = "不良11")
    private Long defect11;

    /** 不良12 */
    @Excel(name = "不良12")
    private Long defect12;

    /** 不良13 */
    @Excel(name = "不良13")
    private Long defect13;

    /** 不良14 */
    @Excel(name = "不良14")
    private Long defect14;

    /** 不良15 */
    @Excel(name = "不良15")
    private Long defect15;

    /** 不良16 */
    @Excel(name = "不良16")
    private Long defect16;

    /** 不良17 */
    @Excel(name = "不良17")
    private Long defect17;

    /** 不良18 */
    @Excel(name = "不良18")
    private Long defect18;

    /** 不良19 */
    @Excel(name = "不良19")
    private Long defect19;

    /** 不良20 */
    @Excel(name = "不良20")
    private Long defect20;
    /** 不良20 */
    @Excel(name = "全部缺陷项")
    private String allDefectItems;
    /** 不良20 */
    @Excel(name = "暂停时间")
    private String stopTime;
    /** 不良20 */
    @Excel(name = "api反馈")
    private String apiDetail;
    @Excel(name = "二维码")
    private String qrCode;
    @Excel(name = "二维码")
    private String apiReport;
    private String color;

    /** 报工记录信息 */
    private List<InspectionReport> inspectionReportList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setWorkOrderCode(String workOrderCode) 
    {
        this.workOrderCode = workOrderCode;
    }

    public String getWorkOrderCode() 
    {
        return workOrderCode;
    }

    public void setTotalQuantity(Long totalQuantity) 
    {
        this.totalQuantity = totalQuantity;
    }

    public Long getTotalQuantity() 
    {
        return totalQuantity;
    }

    public void setDefectiveTotal(Long defectiveTotal) 
    {
        this.defectiveTotal = defectiveTotal;
    }

    public Long getDefectiveTotal() 
    {
        return defectiveTotal;
    }

    public void setDefect1(Long defect1) 
    {
        this.defect1 = defect1;
    }

    public Long getDefect1() 
    {
        return defect1;
    }

    public void setDefect2(Long defect2) 
    {
        this.defect2 = defect2;
    }

    public Long getDefect2() 
    {
        return defect2;
    }

    public void setDefect3(Long defect3) 
    {
        this.defect3 = defect3;
    }

    public Long getDefect3() 
    {
        return defect3;
    }

    public void setDefect4(Long defect4) 
    {
        this.defect4 = defect4;
    }

    public Long getDefect4() 
    {
        return defect4;
    }

    public void setDefect5(Long defect5) 
    {
        this.defect5 = defect5;
    }

    public Long getDefect5() 
    {
        return defect5;
    }

    public void setDefect6(Long defect6) 
    {
        this.defect6 = defect6;
    }

    public Long getDefect6() 
    {
        return defect6;
    }

    public void setDefect7(Long defect7) 
    {
        this.defect7 = defect7;
    }

    public Long getDefect7() 
    {
        return defect7;
    }

    public void setDefect8(Long defect8) 
    {
        this.defect8 = defect8;
    }

    public Long getDefect8() 
    {
        return defect8;
    }

    public void setDefect9(Long defect9) 
    {
        this.defect9 = defect9;
    }

    public Long getDefect9() 
    {
        return defect9;
    }

    public void setDefect10(Long defect10) 
    {
        this.defect10 = defect10;
    }

    public Long getDefect10() 
    {
        return defect10;
    }

    public void setDefect11(Long defect11) 
    {
        this.defect11 = defect11;
    }

    public Long getDefect11() 
    {
        return defect11;
    }

    public void setDefect12(Long defect12) 
    {
        this.defect12 = defect12;
    }

    public Long getDefect12() 
    {
        return defect12;
    }

    public void setDefect13(Long defect13) 
    {
        this.defect13 = defect13;
    }

    public Long getDefect13() 
    {
        return defect13;
    }

    public void setDefect14(Long defect14) 
    {
        this.defect14 = defect14;
    }

    public Long getDefect14() 
    {
        return defect14;
    }

    public void setDefect15(Long defect15) 
    {
        this.defect15 = defect15;
    }

    public Long getDefect15() 
    {
        return defect15;
    }

    public void setDefect16(Long defect16) 
    {
        this.defect16 = defect16;
    }

    public Long getDefect16() 
    {
        return defect16;
    }

    public void setDefect17(Long defect17) 
    {
        this.defect17 = defect17;
    }

    public Long getDefect17() 
    {
        return defect17;
    }

    public void setDefect18(Long defect18) 
    {
        this.defect18 = defect18;
    }

    public Long getDefect18() 
    {
        return defect18;
    }

    public void setDefect19(Long defect19) 
    {
        this.defect19 = defect19;
    }

    public Long getDefect19() 
    {
        return defect19;
    }

    public void setDefect20(Long defect20) 
    {
        this.defect20 = defect20;
    }

    public Long getDefect20() 
    {
        return defect20;
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

    public List<InspectionReport> getInspectionReportList()
    {
        return inspectionReportList;
    }

    public void setInspectionReportList(List<InspectionReport> inspectionReportList)
    {
        this.inspectionReportList = inspectionReportList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("workOrderCode", getWorkOrderCode())
            .append("totalQuantity", getTotalQuantity())
            .append("defectiveTotal", getDefectiveTotal())
            .append("defect1", getDefect1())
            .append("defect2", getDefect2())
            .append("defect3", getDefect3())
            .append("defect4", getDefect4())
            .append("defect5", getDefect5())
            .append("defect6", getDefect6())
            .append("defect7", getDefect7())
            .append("defect8", getDefect8())
            .append("defect9", getDefect9())
            .append("defect10", getDefect10())
            .append("defect11", getDefect11())
            .append("defect12", getDefect12())
            .append("defect13", getDefect13())
            .append("defect14", getDefect14())
            .append("defect15", getDefect15())
            .append("defect16", getDefect16())
            .append("defect17", getDefect17())
            .append("defect18", getDefect18())
            .append("defect19", getDefect19())
            .append("defect20", getDefect20())
                .append("allDefectItems", getAllDefectItems())
                .append("stopTime", getStopTime())
                .append("apiDetail", getApiDetail())
                .append("qrCode", getQrCode())
                .append("apiReport", getApiReport())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("inspectionReportList", getInspectionReportList())
            .toString();
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
}
