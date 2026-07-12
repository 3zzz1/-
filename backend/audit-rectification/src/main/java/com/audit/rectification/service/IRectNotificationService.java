package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectNotification;

/**
 * 审计整改通知Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectNotificationService {

    /**
     * 查询我的通知列表
     *
     * @return 通知集合
     */
    List<RectNotification> selectMyNotificationList();

    /**
     * 标记通知为已读
     *
     * @param ids 通知ID数组
     * @return 结果
     */
    int markAsRead(Long[] ids);

    /**
     * 获取未读通知数量
     *
     * @return 未读数量
     */
    int getUnreadCount();

    /**
     * 新增通知
     *
     * @param notification 通知对象
     * @return 结果
     */
    int insertNotification(RectNotification notification);

    /**
     * 给指定用户发送站内通知。
     */
    int notifyUser(Long userId, Long taskId, Long issueId, String title, String content);

    /**
     * 给指定角色用户发送站内通知。
     */
    int notifyRoles(String[] roleKeys, Long deptId, Long taskId, Long issueId, String title, String content);
}
