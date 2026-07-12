package com.audit.rectification.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectNotification;
import com.audit.rectification.service.IRectNotificationService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 审计整改通知Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/notification")
public class RectNotificationController extends BaseController {

    @Autowired
    private IRectNotificationService rectNotificationService;

    /**
     * 查询我的通知列表
     */
    @GetMapping("/my-list")
    public TableDataInfo myList() {
        startPage();
        List<RectNotification> list = rectNotificationService.selectMyNotificationList();
        return getDataTable(list);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/read/{ids}")
    public AjaxResult read(@PathVariable Long[] ids) {
        return toAjax(rectNotificationService.markAsRead(ids));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public AjaxResult unreadCount() {
        return success(rectNotificationService.getUnreadCount());
    }
}
