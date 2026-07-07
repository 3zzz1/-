package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectReport;

/**
 * 整改报告Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectReportMapper {

    /**
     * 查询报告列表
     *
     * @param report 报告对象
     * @return 报告集合
     */
    public List<RectReport> selectRectReportList(RectReport report);

    /**
     * 查询报告详情
     *
     * @param reportId 报告ID
     * @return 报告对象
     */
    public RectReport selectRectReportById(Long reportId);

    /**
     * 按任务ID查询报告
     *
     * @param taskId 任务ID
     * @return 报告对象
     */
    public RectReport selectRectReportByTaskId(Long taskId);

    /**
     * 新增报告
     *
     * @param report 报告对象
     * @return 结果
     */
    public int insertRectReport(RectReport report);

    /**
     * 修改报告
     *
     * @param report 报告对象
     * @return 结果
     */
    public int updateRectReport(RectReport report);

    /**
     * 批量删除报告
     *
     * @param reportIds 需要删除的报告ID
     * @return 结果
     */
    public int deleteRectReportByIds(Long[] reportIds);
}
