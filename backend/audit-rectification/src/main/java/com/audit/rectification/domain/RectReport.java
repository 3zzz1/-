package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改报告对象 rect_report
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectReport extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 报告ID */
    private Long reportId;

    /** 关联任务ID */
    private Long taskId;

    /** 报告内容 */
    private String reportContent;

    /** 生成报告文件路径 */
    private String generatedPath;

    /** 报告状态（0=草稿 1=已提交 2=单位已审批 3=审计处已接收） */
    private String status;

    /** 提交用户ID */
    private Long submitUserId;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 单位审批状态（0=待审批 1=已通过 2=已驳回） */
    private String unitApproveStatus;

    /** 单位审批人ID */
    private Long unitApproveUserId;

    /** 单位审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date unitApproveTime;

    /** 单位审批意见 */
    private String unitApproveOpinion;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getGeneratedPath() {
        return generatedPath;
    }

    public void setGeneratedPath(String generatedPath) {
        this.generatedPath = generatedPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSubmitUserId() {
        return submitUserId;
    }

    public void setSubmitUserId(Long submitUserId) {
        this.submitUserId = submitUserId;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public String getUnitApproveStatus() {
        return unitApproveStatus;
    }

    public void setUnitApproveStatus(String unitApproveStatus) {
        this.unitApproveStatus = unitApproveStatus;
    }

    public Long getUnitApproveUserId() {
        return unitApproveUserId;
    }

    public void setUnitApproveUserId(Long unitApproveUserId) {
        this.unitApproveUserId = unitApproveUserId;
    }

    public Date getUnitApproveTime() {
        return unitApproveTime;
    }

    public void setUnitApproveTime(Date unitApproveTime) {
        this.unitApproveTime = unitApproveTime;
    }

    public String getUnitApproveOpinion() {
        return unitApproveOpinion;
    }

    public void setUnitApproveOpinion(String unitApproveOpinion) {
        this.unitApproveOpinion = unitApproveOpinion;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectReport{" +
                "reportId=" + reportId +
                ", taskId=" + taskId +
                ", reportContent='" + reportContent + '\'' +
                ", generatedPath='" + generatedPath + '\'' +
                ", status='" + status + '\'' +
                ", submitUserId=" + submitUserId +
                ", submitTime=" + submitTime +
                ", unitApproveStatus='" + unitApproveStatus + '\'' +
                ", unitApproveUserId=" + unitApproveUserId +
                ", unitApproveTime=" + unitApproveTime +
                ", unitApproveOpinion='" + unitApproveOpinion + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
