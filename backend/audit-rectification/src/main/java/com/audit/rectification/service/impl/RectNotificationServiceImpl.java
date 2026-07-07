package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectNotification;
import com.audit.rectification.mapper.RectNotificationMapper;
import com.audit.rectification.service.IRectNotificationService;
import com.ruoyi.common.utils.SecurityUtils;

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
        notification.setSendStatus("0");
        notification.setReadStatus("0");
        notification.setCreateBy(SecurityUtils.getUsername());
        notification.setCreateTime(new Date());
        return rectNotificationMapper.insertRectNotification(notification);
    }
}
