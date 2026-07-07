package com.audit.rectification.domain.dto;

/**
 * 销号审核DTO
 *
 * @author audit
 */
public class ClosureReviewDTO {

    /** 销号申请ID */
    private Long closureId;

    /** 审核结果: 1=整改完成销号 2=整改不到位驳回 */
    private String auditResult;

    /** 审核意见 */
    private String auditOpinion;

    /** 补充整改要求(驳回时填写) */
    private String reRectRequired;

    public Long getClosureId() {
        return closureId;
    }

    public void setClosureId(Long closureId) {
        this.closureId = closureId;
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
}
