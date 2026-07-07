package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改任务对象 rect_task
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 关联问题ID列表（JSON数组文本） */
    private String issueIds;

    /** 整改部门ID */
    private Long rectDeptId;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 整改要求 */
    private String taskRequirement;

    /** 截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    /** 通知书文件路径 */
    private String noticeFilePath;

    /** 状态（0=待确认 1=整改中 2=已提交报告 3=待审核 4=已完成 5=已驳回） */
    private String status;

    /** 下发人ID */
    private Long dispatchUserId;

    /** 下发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dispatchTime;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(String issueIds) {
        this.issueIds = issueIds;
    }

    public Long getRectDeptId() {
        return rectDeptId;
    }

    public void setRectDeptId(Long rectDeptId) {
        this.rectDeptId = rectDeptId;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getTaskRequirement() {
        return taskRequirement;
    }

    public void setTaskRequirement(String taskRequirement) {
        this.taskRequirement = taskRequirement;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getNoticeFilePath() {
        return noticeFilePath;
    }

    public void setNoticeFilePath(String noticeFilePath) {
        this.noticeFilePath = noticeFilePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDispatchUserId() {
        return dispatchUserId;
    }

    public void setDispatchUserId(Long dispatchUserId) {
        this.dispatchUserId = dispatchUserId;
    }

    public Date getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(Date dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectTask{" +
                "taskId=" + taskId +
                ", taskNo='" + taskNo + '\'' +
                ", issueIds='" + issueIds + '\'' +
                ", rectDeptId=" + rectDeptId +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", taskRequirement='" + taskRequirement + '\'' +
                ", deadline=" + deadline +
                ", noticeFilePath='" + noticeFilePath + '\'' +
                ", status='" + status + '\'' +
                ", dispatchUserId=" + dispatchUserId +
                ", dispatchTime=" + dispatchTime +
                ", confirmTime=" + confirmTime +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
