package com.audit.rectification.domain.dto;

import java.util.List;

/**
 * 整改任务下发DTO
 *
 * @author audit
 */
public class TaskDispatchDTO {

    /** 关联问题ID列表 */
    private List<Long> issueIds;

    /** 整改单位ID */
    private Long rectDeptId;

    /** 联络人 */
    private String contactPerson;

    /** 联络电话 */
    private String contactPhone;

    /** 整改要求 */
    private String taskRequirement;

    /** 整改截止日期 */
    private String deadline;

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<Long> issueIds) {
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
