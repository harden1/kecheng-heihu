package com.ruoyi.apiTool.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 黑湖用户信息对象 blacklack_user
 * 
 * @author w
 * @date 2025-07-17
 */
@TableName("blacklack_user")
public class BlacklackUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId() // 主键自增
    private Long id;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 用户姓名 */
    @Excel(name = "用户姓名")
    private String name;

    /** 角色名称（仅取第一个角色） */
    @Excel(name = "角色名称", readConverterExp = "仅=取第一个角色")
    private String roleName;

    /** 部门名称（仅取第一个部门） */
    @Excel(name = "部门名称", readConverterExp = "仅=取第一个部门")
    private String departmentName;

    /** 状态：1=启用，3=停用 */
    @Excel(name = "状态：1=启用，3=停用")
    private Long active;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getUsername() 
    {
        return username;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setRoleName(String roleName) 
    {
        this.roleName = roleName;
    }

    public String getRoleName() 
    {
        return roleName;
    }

    public void setDepartmentName(String departmentName) 
    {
        this.departmentName = departmentName;
    }

    public String getDepartmentName() 
    {
        return departmentName;
    }

    public void setActive(Long active) 
    {
        this.active = active;
    }

    public Long getActive() 
    {
        return active;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("username", getUsername())
            .append("name", getName())
            .append("roleName", getRoleName())
            .append("departmentName", getDepartmentName())
            .append("active", getActive())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
