package com.ruoyi.badItem.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * badItem对象 create_bad_items_table
 * 
 * @author ruoyi
 * @date 2025-06-11
 */
public class CreateBadItemsTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 不良项名称 */
    @Excel(name = "不良项名称")
    private String badName;

    /** 不良项颜色 */
    @Excel(name = "不良项颜色")
    private String badColor;
    @Excel(name = "不良项字体颜色")
    private String fontColor;

    /** 顺序 */
    @Excel(name = "顺序")
    private Long no;

    /** 启用状态 */
    @Excel(name = "启用状态")
    private String state;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBadName(String badName) 
    {
        this.badName = badName;
    }

    public String getBadName() 
    {
        return badName;
    }

    public void setBadColor(String badColor) 
    {
        this.badColor = badColor;
    }

    public String getBadColor() 
    {
        return badColor;
    }

    public void setNo(Long no) 
    {
        this.no = no;
    }

    public Long getNo() 
    {
        return no;
    }

    public void setState(String state) 
    {
        this.state = state;
    }

    public String getState() 
    {
        return state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("badName", getBadName())
            .append("badColor", getBadColor())
            .append("no", getNo())
            .append("state", getState())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }
}
