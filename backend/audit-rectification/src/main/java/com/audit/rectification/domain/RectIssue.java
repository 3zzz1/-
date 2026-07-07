package com.audit.rectification.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

public class RectIssue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Excel(name = "问题ID", cellType = ColumnType.NUMERIC)
    private Long issueId;

    @Excel(name = "问题编号")
    private String issueNo;

    @Excel(name = "来源类型", readConverterExp = "1=内源审计,2=外源巡视巡察,3=外部督查")
    private String sourceType;

    private Long sourceProjectId;

    @Excel(name = "来源描述")
    private String sourceDesc;

    @Excel(name = "问题标题")
    private String issueTitle;

    @Excel(name = "问题描述")
    private String issueDesc;

    @Excel(name = "涉及金额", cellType = ColumnType.NUMERIC)
    private BigDecimal issueAmount;

    @Excel(name = "定性法规依据")
    private String legalBasis;

    private Long responsibleDeptId;

    @Excel(name = "责任干部")
    private String responsiblePerson;

    @Excel(name = "问题分类", readConverterExp = "FUND=资金类,ASSET=资产类,PURCHASE=采购类,HR=人事类,CONSTRUCTION=基建类,OTHER=其他")
    private String issueCategory;

    @Excel(name = "风险等级", readConverterExp = "1=低,2=中,3=高")
    private String riskLevel;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "整改截止日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deadline;

    @Excel(name = "状态", readConverterExp = "0=待下发,1=整改中,2=待审核,3=已销号,4=持续整改")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "销号日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date closureDate;

    private String delFlag;

    // getters and setters
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public String getIssueNo() { return issueNo; }
    public void setIssueNo(String issueNo) { this.issueNo = issueNo; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceProjectId() { return sourceProjectId; }
    public void setSourceProjectId(Long sourceProjectId) { this.sourceProjectId = sourceProjectId; }
    public String getSourceDesc() { return sourceDesc; }
    public void setSourceDesc(String sourceDesc) { this.sourceDesc = sourceDesc; }
    public String getIssueTitle() { return issueTitle; }
    public void setIssueTitle(String issueTitle) { this.issueTitle = issueTitle; }
    public String getIssueDesc() { return issueDesc; }
    public void setIssueDesc(String issueDesc) { this.issueDesc = issueDesc; }
    public BigDecimal getIssueAmount() { return issueAmount; }
    public void setIssueAmount(BigDecimal issueAmount) { this.issueAmount = issueAmount; }
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    public Long getResponsibleDeptId() { return responsibleDeptId; }
    public void setResponsibleDeptId(Long responsibleDeptId) { this.responsibleDeptId = responsibleDeptId; }
    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }
    public String getIssueCategory() { return issueCategory; }
    public void setIssueCategory(String issueCategory) { this.issueCategory = issueCategory; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getClosureDate() { return closureDate; }
    public void setClosureDate(Date closureDate) { this.closureDate = closureDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
