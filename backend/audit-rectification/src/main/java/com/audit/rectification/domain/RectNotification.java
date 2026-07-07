package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改通知对象 rect_notification
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectNotification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long notificationId;

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long issueId;

    /** 通知类型（SYSTEM_MSG/SMS/WECHAT） */
    private String notifyType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 接收人用户ID */
    private Long recipientUserId;

    /** 接收人手机号 */
    private String recipientPhone;

    /** 发送状态（0=待发送 1=已发送 2=失败） */
    private String sendStatus;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /** 阅读状态（0=未读 1=已读） */
    private String readStatus;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    public String toString() {
        return "RectNotification{" +
                "notificationId=" + notificationId +
                ", taskId=" + taskId +
                ", issueId=" + issueId +
                ", notifyType='" + notifyType + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", recipientUserId=" + recipientUserId +
                ", recipientPhone='" + recipientPhone + '\'' +
                ", sendStatus='" + sendStatus + '\'' +
                ", sendTime=" + sendTime +
                ", readStatus='" + readStatus + '\'' +
                ", readTime=" + readTime +
                '}';
    }
}
