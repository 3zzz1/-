package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectClosure;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectReport;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.mapper.RectClosureMapper;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.mapper.RectReportMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.ruoyi.common.exception.ServiceException;
import com.audit.rectification.service.IRectClosureService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改销号Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectClosureServiceImpl implements IRectClosureService {

    @Autowired
    private RectClosureMapper rectClosureMapper;

    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Autowired
    private RectTaskMapper rectTaskMapper;

    @Autowired
    private RectReportMapper rectReportMapper;

    @Override
    public List<RectClosure> selectRectClosureList(RectClosure closure) {
        return rectClosureMapper.selectRectClosureList(closure);
    }

    @Override
    public RectClosure selectRectClosureById(Long closureId) {
        return rectClosureMapper.selectRectClosureById(closureId);
    }

    @Override
    public RectClosure selectLatestRectClosureByTaskId(Long taskId) {
        return rectClosureMapper.selectLatestRectClosureByTaskId(taskId);
    }

    @Override
    @Transactional
    public int applyClosure(Long issueId, Long taskId, String content) {
        RectReport report = rectReportMapper.selectRectReportByTaskId(taskId);
        if (report == null || !"2".equals(report.getStatus()) || !"1".equals(report.getUnitApproveStatus())) {
            throw new ServiceException("整改报告需经被审单位负责人审批通过后，方可发起销号申请");
        }

        // 作废同一任务下仍在待审核的旧销号申请，避免管理员继续处理旧请求。
        RectClosure query = new RectClosure();
        query.setTaskId(taskId);
        query.setStatus("0");
        List<RectClosure> pendingClosures = rectClosureMapper.selectRectClosureList(query);
        for (RectClosure pending : pendingClosures) {
            RectClosure old = new RectClosure();
            old.setClosureId(pending.getClosureId());
            old.setStatus("2");
            old.setAuditResult("2");
            old.setAuditOpinion("整改单位重新发起销号申请，原待审核销号申请自动作废。");
            old.setReRectRequired("以最新销号申请为准。");
            old.setAuditUserId(SecurityUtils.getUserId());
            old.setAuditTime(new Date());
            old.setUpdateBy(SecurityUtils.getUsername());
            old.setUpdateTime(new Date());
            rectClosureMapper.updateRectClosure(old);
        }

        RectClosure newClosure = new RectClosure();
        newClosure.setIssueId(issueId);
        newClosure.setTaskId(taskId);
        newClosure.setApplyContent(content);
        newClosure.setStatus("0");
        newClosure.setApplyUserId(SecurityUtils.getUserId());
        newClosure.setApplyTime(new Date());
        newClosure.setCreateBy(SecurityUtils.getUsername());
        newClosure.setCreateTime(new Date());
        int rows = rectClosureMapper.insertRectClosure(newClosure);

        // 更新问题状态为"待审核"
        RectIssue issue = new RectIssue();
        issue.setIssueId(issueId);
        issue.setStatus("2");
        issue.setUpdateBy(SecurityUtils.getUsername());
        issue.setUpdateTime(new Date());
        rectIssueMapper.updateRectIssue(issue);

        // 同步更新任务状态为"待审核"
        RectTask t = new RectTask();
        t.setTaskId(taskId);
        t.setStatus("3");
        t.setUpdateBy(SecurityUtils.getUsername());
        t.setUpdateTime(new Date());
        rectTaskMapper.updateRectTask(t);

        // 创建进度记录
        RectProgress progress = new RectProgress();
        progress.setTaskId(taskId);
        progress.setIssueId(issueId);
        progress.setProgressType("CLOSURE_APPLY");
        progress.setContent("申请销号：" + content);
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);

        return rows;
    }

    @Override
    @Transactional
    public int auditClosure(Long closureId, String result, String opinion, String reRectRequired) {
        // 查询销号记录
        RectClosure existingClosure = rectClosureMapper.selectRectClosureById(closureId);

        // 更新销号记录
        RectClosure closure = new RectClosure();
        closure.setClosureId(closureId);
        closure.setAuditResult(result);
        closure.setAuditOpinion(opinion);
        closure.setAuditUserId(SecurityUtils.getUserId());
        closure.setAuditTime(new Date());
        closure.setUpdateBy(SecurityUtils.getUsername());
        closure.setUpdateTime(new Date());

        if ("1".equals(result)) {
            // 审批通过：销号
            closure.setStatus("1");

            // 更新问题状态为"已销号"，设置销号日期
            RectIssue issue = new RectIssue();
            issue.setIssueId(existingClosure.getIssueId());
            issue.setStatus("3");
            issue.setClosureDate(new Date());
            issue.setUpdateBy(SecurityUtils.getUsername());
            issue.setUpdateTime(new Date());
            rectIssueMapper.updateRectIssue(issue);

            // 更新任务状态为"已完成"
            RectTask task = new RectTask();
            task.setTaskId(existingClosure.getTaskId());
            task.setStatus("4");
            task.setUpdateBy(SecurityUtils.getUsername());
            task.setUpdateTime(new Date());
            rectTaskMapper.updateRectTask(task);

            // 创建进度记录
            RectProgress progress = new RectProgress();
            progress.setTaskId(existingClosure.getTaskId());
            progress.setIssueId(existingClosure.getIssueId());
            progress.setProgressType("CLOSURE_APPROVED");
            progress.setContent("销号审核通过，问题已销号。审核意见：" + opinion);
            progress.setOperatorId(SecurityUtils.getUserId());
            progress.setOperatorName(SecurityUtils.getUsername());
            progress.setOperateTime(new Date());
            progress.setCreateBy(SecurityUtils.getUsername());
            progress.setCreateTime(new Date());
            rectProgressMapper.insertRectProgress(progress);
        } else {
            // 审批驳回：退回整改
            closure.setStatus("2");
            closure.setReRectRequired(reRectRequired);

            // 更新问题状态为"整改中"
            RectIssue issue = new RectIssue();
            issue.setIssueId(existingClosure.getIssueId());
            issue.setStatus("1");
            issue.setUpdateBy(SecurityUtils.getUsername());
            issue.setUpdateTime(new Date());
            rectIssueMapper.updateRectIssue(issue);

            // 同时更新任务状态为"整改中"，让责任人重新看到
            RectTask task = new RectTask();
            task.setTaskId(existingClosure.getTaskId());
            task.setStatus("1");
            task.setUpdateBy(SecurityUtils.getUsername());
            task.setUpdateTime(new Date());
            rectTaskMapper.updateRectTask(task);

            // 销号被驳回后，原整改报告不再代表最终整改结果，需要退回可编辑状态，
            // 由整改人补充整改后重新提交给被审单位负责人审批。
            RectReport report = rectReportMapper.selectRectReportByTaskId(existingClosure.getTaskId());
            if (report != null) {
                RectReport updateReport = new RectReport();
                updateReport.setReportId(report.getReportId());
                updateReport.setStatus("0");
                updateReport.setUnitApproveStatus("0");
                updateReport.setUpdateBy(SecurityUtils.getUsername());
                updateReport.setUpdateTime(new Date());
                rectReportMapper.updateRectReport(updateReport);
            }

            // 创建进度记录
            RectProgress progress = new RectProgress();
            progress.setTaskId(existingClosure.getTaskId());
            progress.setIssueId(existingClosure.getIssueId());
            progress.setProgressType("CLOSURE_REJECTED");
            progress.setContent("销号审核驳回，需重新整改。补充整改要求："
                    + (reRectRequired != null ? reRectRequired : ""));
            progress.setOperatorId(SecurityUtils.getUserId());
            progress.setOperatorName(SecurityUtils.getUsername());
            progress.setOperateTime(new Date());
            progress.setCreateBy(SecurityUtils.getUsername());
            progress.setCreateTime(new Date());
            rectProgressMapper.insertRectProgress(progress);
        }

        return rectClosureMapper.updateRectClosure(closure);
    }
}
