package com.ruoyi.inspection.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 镜检单条记录对象 inspection_record
 * 
 * @author w
 * @date 2025-07-08
 */
public class InspectionRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 主表ID（inspection_summary.id） */
    @Excel(name = "主表ID", readConverterExp = "i=nspection_summary.id")
    private Long summaryId;

    /** 不良项名称 */
    @Excel(name = "不良项名称")
    private String defectName;

    /** 上传标志，true表示已上传 */
    @Excel(name = "上传标志，true表示已上传")
    private Integer uploadFlag;

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

    public void setDefectName(String defectName) 
    {
        this.defectName = defectName;
    }

    public String getDefectName() 
    {
        return defectName;
    }

    public void setUploadFlag(Integer uploadFlag) 
    {
        this.uploadFlag = uploadFlag;
    }

    public Integer getUploadFlag() 
    {
        return uploadFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("summaryId", getSummaryId())
            .append("defectName", getDefectName())
            .append("uploadFlag", getUploadFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
