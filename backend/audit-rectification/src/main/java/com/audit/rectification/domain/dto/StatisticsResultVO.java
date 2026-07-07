package com.audit.rectification.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计分析结果VO
 *
 * @author audit
 */
public class StatisticsResultVO {

    /** 整改问题总数 */
    private Long totalIssues;

    /** 已完成(已销号)数量 */
    private Long completedCount;

    /** 整改中数量 */
    private Long inProgressCount;

    /** 逾期数量 */
    private Long overdueCount;

    /** 整改完成率 */
    private BigDecimal completionRate;

    /** 逾期率 */
    private BigDecimal overdueRate;

    /** 资金挽回总额 */
    private BigDecimal totalRecoveryAmount;

    /** 按分类统计 */
    private List<Map<String, Object>> categoryDistribution;

    /** 按状态统计 */
    private List<Map<String, Object>> statusDistribution;

    /** 按部门统计 */
    private List<Map<String, Object>> deptDistribution;

    /** 整改趋势数据(按月) */
    private List<Map<String, Object>> monthlyTrend;

    /** 高频风险领域 */
    private List<Map<String, Object>> highRiskAreas;

    /** 重复发生问题 */
    private List<Map<String, Object>> recurringIssues;

    public Long getTotalIssues() {
        return totalIssues;
    }

    public void setTotalIssues(Long totalIssues) {
        this.totalIssues = totalIssues;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public Long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(Long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public Long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public BigDecimal getOverdueRate() {
        return overdueRate;
    }

    public void setOverdueRate(BigDecimal overdueRate) {
        this.overdueRate = overdueRate;
    }

    public BigDecimal getTotalRecoveryAmount() {
        return totalRecoveryAmount;
    }

    public void setTotalRecoveryAmount(BigDecimal totalRecoveryAmount) {
        this.totalRecoveryAmount = totalRecoveryAmount;
    }

    public List<Map<String, Object>> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<Map<String, Object>> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<Map<String, Object>> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(List<Map<String, Object>> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<Map<String, Object>> getDeptDistribution() {
        return deptDistribution;
    }

    public void setDeptDistribution(List<Map<String, Object>> deptDistribution) {
        this.deptDistribution = deptDistribution;
    }

    public List<Map<String, Object>> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<Map<String, Object>> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public List<Map<String, Object>> getHighRiskAreas() {
        return highRiskAreas;
    }

    public void setHighRiskAreas(List<Map<String, Object>> highRiskAreas) {
        this.highRiskAreas = highRiskAreas;
    }

    public List<Map<String, Object>> getRecurringIssues() {
        return recurringIssues;
    }

    public void setRecurringIssues(List<Map<String, Object>> recurringIssues) {
        this.recurringIssues = recurringIssues;
    }
}
