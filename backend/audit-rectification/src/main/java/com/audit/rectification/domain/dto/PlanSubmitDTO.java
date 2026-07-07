package com.audit.rectification.domain.dto;

/**
 * 整改方案提交DTO
 *
 * @author audit
 */
public class PlanSubmitDTO {

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long issueId;

    /** 方案内容(富文本) */
    private String planContent;

    /** 整改责任人ID */
    private Long responsibleUserId;

    /** 计划完成日期 */
    private String planDeadline;

    /** 整改类型: 1=正常 2=长期持续 */
    private String planType;

    /** 长期整改原因 */
    private String longTermReason;

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

    public String getPlanContent() {
        return planContent;
    }

    public void setPlanContent(String planContent) {
        this.planContent = planContent;
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getPlanDeadline() {
        return planDeadline;
    }

    public void setPlanDeadline(String planDeadline) {
        this.planDeadline = planDeadline;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getLongTermReason() {
        return longTermReason;
    }

    public void setLongTermReason(String longTermReason) {
        this.longTermReason = longTermReason;
    }
}
