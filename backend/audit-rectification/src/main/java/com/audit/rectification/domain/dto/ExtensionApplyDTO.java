package com.audit.rectification.domain.dto;

/**
 * 延期申请DTO
 *
 * @author audit
 */
public class ExtensionApplyDTO {

    /** 问题ID */
    private Long issueId;

    /** 任务ID */
    private Long taskId;

    /** 原截止日期 */
    private String originalDeadline;

    /** 延期天数 */
    private Integer extensionDays;

    /** 新截止日期 */
    private String newDeadline;

    /** 延期原因 */
    private String reason;

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

    public String getOriginalDeadline() {
        return originalDeadline;
    }

    public void setOriginalDeadline(String originalDeadline) {
        this.originalDeadline = originalDeadline;
    }

    public Integer getExtensionDays() {
        return extensionDays;
    }

    public void setExtensionDays(Integer extensionDays) {
        this.extensionDays = extensionDays;
    }

    public String getNewDeadline() {
        return newDeadline;
    }

    public void setNewDeadline(String newDeadline) {
        this.newDeadline = newDeadline;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
