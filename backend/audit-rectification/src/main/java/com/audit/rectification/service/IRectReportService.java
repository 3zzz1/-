package com.audit.rectification.service;

import com.audit.rectification.domain.RectReport;

/**
 * 审计整改报告Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectReportService {

    /**
     * 按任务ID查询整改报告
     *
     * @param taskId 任务ID
     * @return 报告对象
     */
    RectReport selectRectReportByTaskId(Long taskId);

    /**
     * 新增整改报告
     *
     * @param report 报告对象
     * @return 结果
     */
    int insertRectReport(RectReport report);

    /**
     * 修改整改报告
     *
     * @param report 报告对象
     * @return 结果
     */
    int updateRectReport(RectReport report);

    /**
     * 生成整改报告（Word文档）
     *
     * @param taskId 任务ID
     * @return 生成结果（文件路径）
     */
    String generateReport(Long taskId);

    /**
     * 提交报告审批
     *
     * @param reportId 报告ID
     * @return 结果
     */
    int submitForApproval(Long reportId);

    /**
     * 领导审批报告
     *
     * @param reportId 报告ID
     * @param opinion 审批意见
     * @param status 审批状态
     * @return 结果
     */
    int leaderApprove(Long reportId, String opinion, String status);
}
