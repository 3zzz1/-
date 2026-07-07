package com.audit.rectification.domain.dto;

/**
 * 统计分析查询DTO
 *
 * @author audit
 */
public class StatisticsQueryDTO {

    /** 统计开始日期 */
    private String beginTime;

    /** 统计结束日期 */
    private String endTime;

    /** 统计维度: YEAR/MONTH/DEPT/CATEGORY */
    private String dimension;

    /** 部门ID(按部门统计时使用) */
    private Long deptId;

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

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
