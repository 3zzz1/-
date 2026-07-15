package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectNotification;
import com.audit.rectification.mapper.RectNotificationMapper;
import com.audit.rectification.service.IRectNotificationService;
import com.audit.rectification.service.IWeComMessageService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * 审计整改通知Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectNotificationServiceImpl implements IRectNotificationService {

    @Autowired
    private RectNotificationMapper rectNotificationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IWeComMessageService weComMessageService;

    @Override
    public List<RectNotification> selectMyNotificationList() {
        RectNotification query = new RectNotification();
        query.setRecipientUserId(SecurityUtils.getUserId());
        return rectNotificationMapper.selectRectNotificationList(query);
    }

    @Override
    @Transactional
    public int markAsRead(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            RectNotification notification = new RectNotification();
            notification.setNotificationId(id);
            notification.setRecipientUserId(SecurityUtils.getUserId());
            notification.setReadStatus("1");
            notification.setReadTime(new Date());
            rows += rectNotificationMapper.updateRectNotification(notification);
        }
        return rows;
    }

    @Override
    public int getUnreadCount() {
        return rectNotificationMapper.selectUnreadCount(SecurityUtils.getUserId());
    }

    @Override
    @Transactional
    public int insertNotification(RectNotification notification) {
        notification.setSendStatus("1");
        notification.setSendTime(new Date());
        notification.setReadStatus("0");
        notification.setCreateBy(SecurityUtils.getUsername());
        notification.setCreateTime(new Date());
        return rectNotificationMapper.insertRectNotification(notification);
    }

    @Override
    @Transactional
    public int notifyUser(Long userId, Long taskId, Long issueId, String title, String content) {
        if (userId == null) {
            return 0;
        }
        RectNotification notification = new RectNotification();
        notification.setRecipientUserId(userId);
        notification.setTaskId(taskId);
        notification.setIssueId(issueId);
        notification.setNotifyType("SYSTEM_MSG");
        notification.setTitle(title);
        notification.setContent(content);
        int rows = insertNotification(notification);
        SysUser user = sysUserMapper.selectUserById(userId);
        weComMessageService.sendTaskNotice(user, taskId, title, content);
        return rows;
    }

    @Override
    @Transactional
    public int notifyRoles(String[] roleKeys, Long deptId, Long taskId, Long issueId, String title, String content) {
        if (roleKeys == null || roleKeys.length == 0) {
            return 0;
        }
        List<SysUser> users = sysUserMapper.selectUsersByRoleKeys(roleKeys, deptId);
        int rows = 0;
        Set<Long> notifiedUserIds = new HashSet<>();
        if (users != null) {
            for (SysUser user : users) {
                if (user.getUserId() != null && notifiedUserIds.add(user.getUserId())) {
                    rows += notifyUser(user.getUserId(), taskId, issueId, title, content);
                }
            }
        }
        return rows;
    }
}
