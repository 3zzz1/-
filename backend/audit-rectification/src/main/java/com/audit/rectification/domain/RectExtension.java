package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改延期申请对象 rect_extension
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectExtension extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 延期ID */
    private Long extensionId;

    /** 关联问题ID */
    private Long issueId;

    /** 关联任务ID */
    private Long taskId;

    /** 申请类型（1=延期 2=长期持续整改） */
    private String applyType;

    /** 原截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date originalDeadline;

    /** 延期天数 */
    private Integer extensionDays;

    /** 新截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date newDeadline;

    /** 延期原因 */
    private String reason;

    /** 阶段整改目标 */
    private String stageGoal;

    /** 下一次复核日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date reviewDate;

    /** 预计最终完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedFinishDate;

    /** 状态（0=单位审批中 1=审计处审批中 2=已通过 3=单位驳回 4=审计处驳回） */
    private String status;

    /** 申请人ID */
    private Long applyUserId;

    /** 申请人真实姓名 */
    private String applyUserName;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 审批人ID */
    private Long approveUserId;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    private String approveOpinion;

    /** 审计处审批人ID */
    private Long auditApproveUserId;

    /** 审计处审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditApproveTime;

    /** 审计处审批意见 */
    private String auditApproveOpinion;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(Long extensionId) {
        this.extensionId = extensionId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public Date getOriginalDeadline() {
        return originalDeadline;
    }

    public void setOriginalDeadline(Date originalDeadline) {
        this.originalDeadline = originalDeadline;
    }

    public Integer getExtensionDays() {
        return extensionDays;
    }

    public void setExtensionDays(Integer extensionDays) {
        this.extensionDays = extensionDays;
    }

    public Date getNewDeadline() {
        return newDeadline;
    }

    public void setNewDeadline(Date newDeadline) {
        this.newDeadline = newDeadline;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStageGoal() {
        return stageGoal;
    }

    public void setStageGoal(String stageGoal) {
        this.stageGoal = stageGoal;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Date getExpectedFinishDate() {
        return expectedFinishDate;
    }

    public void setExpectedFinishDate(Date expectedFinishDate) {
        this.expectedFinishDate = expectedFinishDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Long getApproveUserId() {
        return approveUserId;
    }

    public void setApproveUserId(Long approveUserId) {
        this.approveUserId = approveUserId;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveOpinion() {
        return approveOpinion;
    }

    public void setApproveOpinion(String approveOpinion) {
        this.approveOpinion = approveOpinion;
    }

    public Long getAuditApproveUserId() {
        return auditApproveUserId;
    }

    public void setAuditApproveUserId(Long auditApproveUserId) {
        this.auditApproveUserId = auditApproveUserId;
    }

    public Date getAuditApproveTime() {
        return auditApproveTime;
    }

    public void setAuditApproveTime(Date auditApproveTime) {
        this.auditApproveTime = auditApproveTime;
    }

    public String getAuditApproveOpinion() {
        return auditApproveOpinion;
    }

    public void setAuditApproveOpinion(String auditApproveOpinion) {
        this.auditApproveOpinion = auditApproveOpinion;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectExtension{" +
                "extensionId=" + extensionId +
                ", issueId=" + issueId +
                ", taskId=" + taskId +
                ", originalDeadline=" + originalDeadline +
                ", extensionDays=" + extensionDays +
                ", newDeadline=" + newDeadline +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", applyUserId=" + applyUserId +
                ", applyTime=" + applyTime +
                ", approveUserId=" + approveUserId +
                ", approveTime=" + approveTime +
                ", approveOpinion='" + approveOpinion + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
