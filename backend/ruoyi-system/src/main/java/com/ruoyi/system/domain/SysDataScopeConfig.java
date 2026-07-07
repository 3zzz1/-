package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 数据权限范围配置表 sys_data_scope_config
 *
 * @author audit
 */
public class SysDataScopeConfig {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;
    private String scopeField;
    private String scopeValue;
    private String scopeTable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getScopeField() { return scopeField; }
    public void setScopeField(String scopeField) { this.scopeField = scopeField; }
    public String getScopeValue() { return scopeValue; }
    public void setScopeValue(String scopeValue) { this.scopeValue = scopeValue; }
    public String getScopeTable() { return scopeTable; }
    public void setScopeTable(String scopeTable) { this.scopeTable = scopeTable; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
