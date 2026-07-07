package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改销号对象 rect_closure
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectClosure extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 销号ID */
    private Long closureId;

    /** 关联问题ID */
    private Long issueId;

    /** 关联任务ID */
    private Long taskId;

    /** 申请内容 */
    private String applyContent;

    /** 状态（0=待审核 1=已销号 2=已驳回） */
    private String status;

    /** 申请人ID */
    private Long applyUserId;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核结果（1=完成销号 2=不到位驳回） */
    private String auditResult;

    /** 审核意见 */
    private String auditOpinion;

    /** 是否需要重新整改 */
    private String reRectRequired;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getClosureId() {
        return closureId;
    }

    public void setClosureId(Long closureId) {
        this.closureId = closureId;
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

    public String getApplyContent() {
        return applyContent;
    }

    public void setApplyContent(String applyContent) {
        this.applyContent = applyContent;
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

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Long getAuditUserId() {
        return auditUserId;
    }

    public void setAuditUserId(Long auditUserId) {
        this.auditUserId = auditUserId;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(String auditResult) {
        this.auditResult = auditResult;
    }

    public String getAuditOpinion() {
        return auditOpinion;
    }

    public void setAuditOpinion(String auditOpinion) {
        this.auditOpinion = auditOpinion;
    }

    public String getReRectRequired() {
        return reRectRequired;
    }

    public void setReRectRequired(String reRectRequired) {
        this.reRectRequired = reRectRequired;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectClosure{" +
                "closureId=" + closureId +
                ", issueId=" + issueId +
                ", taskId=" + taskId +
                ", applyContent='" + applyContent + '\'' +
                ", status='" + status + '\'' +
                ", applyUserId=" + applyUserId +
                ", applyTime=" + applyTime +
                ", auditUserId=" + auditUserId +
                ", auditTime=" + auditTime +
                ", auditResult='" + auditResult + '\'' +
                ", auditOpinion='" + auditOpinion + '\'' +
                ", reRectRequired='" + reRectRequired + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
