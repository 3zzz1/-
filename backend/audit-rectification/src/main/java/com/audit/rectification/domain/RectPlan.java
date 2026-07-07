package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改方案对象 rect_plan
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectPlan extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 方案ID */
    private Long planId;

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long issueId;

    /** 方案内容 */
    private String planContent;

    /** 责任人用户ID */
    private Long responsibleUserId;

    /** 计划完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planDeadline;

    /** 方案类型（1=正常 2=长期持续） */
    private String planType;

    /** 长期持续原因 */
    private String longTermReason;

    /** 状态（0=草稿 1=已提交） */
    private String status;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
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

    public Date getPlanDeadline() {
        return planDeadline;
    }

    public void setPlanDeadline(Date planDeadline) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectPlan{" +
                "planId=" + planId +
                ", taskId=" + taskId +
                ", issueId=" + issueId +
                ", planContent='" + planContent + '\'' +
                ", responsibleUserId=" + responsibleUserId +
                ", planDeadline=" + planDeadline +
                ", planType='" + planType + '\'' +
                ", longTermReason='" + longTermReason + '\'' +
                ", status='" + status + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
