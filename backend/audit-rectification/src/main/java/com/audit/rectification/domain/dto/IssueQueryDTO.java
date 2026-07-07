package com.audit.rectification.domain.dto;

import com.audit.rectification.domain.RectIssue;

/**
 * 问题台账查询DTO
 *
 * @author audit
 */
public class IssueQueryDTO extends RectIssue {

    /** 查询开始日期 */
    private String beginTime;

    /** 查询结束日期 */
    private String endTime;

    /** 逾期筛选: ALL/OVERDUE/NOT_OVERDUE */
    private String overdueFilter;

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getOverdueFilter() {
        return overdueFilter;
    }

    public void setOverdueFilter(String overdueFilter) {
        this.overdueFilter = overdueFilter;
    }
}
