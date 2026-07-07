package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysPermissionAuditLog;

/**
 * 权限审计日志Mapper接口
 *
 * @author audit
 */
public interface SysPermissionAuditLogMapper {

    /**
     * 查询审计日志列表
     */
    public List<SysPermissionAuditLog> selectSysPermissionAuditLogList(SysPermissionAuditLog log);

    /**
     * 新增审计日志
     */
    public int insertSysPermissionAuditLog(SysPermissionAuditLog log);
}
