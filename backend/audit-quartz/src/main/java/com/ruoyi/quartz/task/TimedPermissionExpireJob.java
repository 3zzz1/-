package com.ruoyi.quartz.task;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.SysPermissionAuditLog;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.domain.SysUserRoleTimed;
import com.ruoyi.system.mapper.SysPermissionAuditLogMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.mapper.SysUserRoleTimedMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * 过期权限回收定时任务
 * 每日凌晨2点自动扫描sys_user_role_timed表，
 * 将已到期的时效角色标记为过期，并从sys_user_role中移除对应关联
 * 同时记录审计日志
 *
 * @author audit
 */
@Component
public class TimedPermissionExpireJob
{
    private static final Logger log = LoggerFactory.getLogger(TimedPermissionExpireJob.class);

    /** 过期操作的审计日志detail模板 */
    private static final String AUDIT_DETAIL_TEMPLATE =
            "定时任务自动回收过期权限: userId={}, roleId={}, 原过期时间={}, 回收时间={}";

    @Autowired
    private SysUserRoleTimedMapper sysUserRoleTimedMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysPermissionAuditLogMapper sysPermissionAuditLogMapper;

    /**
     * 定时回收过期权限
     * cron: 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireTimedPermissions()
    {
        log.info("========== 开始执行过期时效权限回收任务 ==========");

        try
        {
            // 1. 查询所有有效但已过期的时效角色分配
            //    status='0'=有效，expire_time < now = 已过期
            List<SysUserRoleTimed> expiredList = sysUserRoleTimedMapper.selectExpiredRecords(new Date());

            if (StringUtils.isEmpty(expiredList))
            {
                log.info("未发现过期的时效权限记录，任务结束。");
                return;
            }

            log.info("发现 {} 条过期的时效权限记录，开始处理...", expiredList.size());

            int expiredCount = 0;
            int revokeCount = 0;
            int auditLogCount = 0;

            for (SysUserRoleTimed timed : expiredList)
            {
                try
                {
                    Long userId = timed.getUserId();
                    Long roleId = timed.getRoleId();
                    Long timedId = timed.getId();

                    if (userId == null || roleId == null)
                    {
                        log.warn("过期记录[{}]缺少userId或roleId，跳过处理。", timedId);
                        continue;
                    }

                    // 2. 更新sys_user_role_timed记录状态为'1'（已过期），记录回收时间
                    timed.setStatus("1");
                    timed.setRevokeTime(new Date());
                    int updateRows = sysUserRoleTimedMapper.updateById(timed);
                    if (updateRows > 0)
                    {
                        expiredCount++;
                        log.debug("已标记时效角色为过期: userId={}, roleId={}, timedId={}",
                                userId, roleId, timedId);
                    }

                    // 3. 从sys_user_role表中删除对应的用户-角色关联
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);

                    int deleteRows = sysUserRoleMapper.deleteUserRoleInfo(userRole);
                    if (deleteRows > 0)
                    {
                        revokeCount++;
                        log.debug("已从sys_user_role移除关联: userId={}, roleId={}",
                                userId, roleId);
                    }
                    else
                    {
                        log.info("sys_user_role中未找到userId={}, roleId={}的关联记录，跳过删除。",
                                userId, roleId);
                    }

                    // 4. 记录审计日志
                    SysPermissionAuditLog auditLog = new SysPermissionAuditLog();
                    auditLog.setUserId(userId);
                    auditLog.setUserName("SYSTEM");
                    auditLog.setOperationType("EXPIRE");
                    auditLog.setTargetType("ROLE");
                    auditLog.setTargetId(roleId);
                    auditLog.setOperationDetail(StringUtils.format(
                            AUDIT_DETAIL_TEMPLATE,
                            userId,
                            roleId,
                            timed.getExpireTime(),
                            new Date()));
                    auditLog.setOperationTime(new Date());
                    auditLog.setCreateTime(new Date());

                    int auditRows = sysPermissionAuditLogMapper.insert(auditLog);
                    if (auditRows > 0)
                    {
                        auditLogCount++;
                    }

                }
                catch (Exception e)
                {
                    log.error("处理过期时效权限记录[{}]时发生异常: {}", timed.getId(), e.getMessage(), e);
                    // 单条记录异常不影响其他记录处理
                }
            }

            log.info("过期时效权限回收任务完成。标记过期: {}条, 移除关联: {}条, 审计日志: {}条",
                    expiredCount, revokeCount, auditLogCount);

        }
        catch (Exception e)
        {
            log.error("过期时效权限回收任务执行失败", e);
            throw e;
        }

        log.info("========== 过期时效权限回收任务结束 ==========");
    }
}
