package com.ruoyi.inspection.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 报工记录对象 inspection_report
 * 
 * @author w
 * @date 2025-07-08
 */
public class InspectionReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 主表ID（inspection_summary.id） */
    @Excel(name = "主表ID", readConverterExp = "i=nspection_summary.id")
    private Long summaryId;

    /** 工单号 */
    @Excel(name = "工单号")
    private String workOrderCode;

    /** 报工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报工时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reportTime;

    /** 报工类型，如“自动”、“手动”等 */
    @Excel(name = "报工类型，如“自动”、“手动”等")
    private String reportType;

    /** 报工数量 */
    @Excel(name = "报工数量")
    private BigDecimal quantity;

    /** 返回json内容 */
    @Excel(name = "返回json内容")
    private String resultJson;

    /** 成功状态，true表示成功 */
    @Excel(name = "成功状态，true表示成功")
    private Integer successFlag;

    /** 不良项备注 */
    @Excel(name = "不良项备注")
    private String defectRemark;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setSummaryId(Long summaryId) 
    {
        this.summaryId = summaryId;
    }

    public Long getSummaryId() 
    {
        return summaryId;
    }

    public void setWorkOrderCode(String workOrderCode) 
    {
        this.workOrderCode = workOrderCode;
    }

    public String getWorkOrderCode() 
    {
        return workOrderCode;
    }

    public void setReportTime(Date reportTime) 
    {
        this.reportTime = reportTime;
    }

    public Date getReportTime() 
    {
        return reportTime;
    }

    public void setReportType(String reportType) 
    {
        this.reportType = reportType;
    }

    public String getReportType() 
    {
        return reportType;
    }

    public void setQuantity(BigDecimal quantity) 
    {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity() 
    {
        return quantity;
    }

    public void setResultJson(String resultJson) 
    {
        this.resultJson = resultJson;
    }

    public String getResultJson() 
    {
        return resultJson;
    }

    public void setSuccessFlag(Integer successFlag) 
    {
        this.successFlag = successFlag;
    }

    public Integer getSuccessFlag() 
    {
        return successFlag;
    }

    public void setDefectRemark(String defectRemark) 
    {
        this.defectRemark = defectRemark;
    }

    public String getDefectRemark() 
    {
        return defectRemark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("summaryId", getSummaryId())
            .append("workOrderCode", getWorkOrderCode())
            .append("reportTime", getReportTime())
            .append("reportType", getReportType())
            .append("quantity", getQuantity())
            .append("resultJson", getResultJson())
            .append("successFlag", getSuccessFlag())
            .append("defectRemark", getDefectRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
