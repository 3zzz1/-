package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.*;
import com.audit.rectification.mapper.*;
import com.audit.rectification.service.IRectReportService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectReportServiceImpl implements IRectReportService {

    @Autowired
    private RectReportMapper rectReportMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;
    @Autowired
    private RectPlanMapper rectPlanMapper;
    @Autowired
    private RectTaskMapper rectTaskMapper;
    @Autowired
    private RectMaterialMapper rectMaterialMapper;
    @Autowired
    private RectClosureMapper rectClosureMapper;
    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Override
    public RectReport selectRectReportByTaskId(Long taskId) {
        return rectReportMapper.selectRectReportByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertRectReport(RectReport report) {
        RectReport existing = report.getTaskId() != null ? rectReportMapper.selectRectReportByTaskId(report.getTaskId()) : null;
        if (existing != null) {
            RectReport query = new RectReport();
            query.setTaskId(report.getTaskId());
            List<RectReport> activeReports = rectReportMapper.selectRectReportList(query);
            if (activeReports != null) {
                for (RectReport activeReport : activeReports) {
                    if (activeReport.getReportId() != null && !activeReport.getReportId().equals(existing.getReportId())) {
                        rectReportMapper.deleteRectReportByIds(new Long[] { activeReport.getReportId() });
                    }
                }
            }
            report.setReportId(existing.getReportId());
            report.setStatus("0");
            report.setUnitApproveStatus("0");
            return updateRectReport(report);
        }
        report.setCreateBy(SecurityUtils.getUsername());
        report.setCreateTime(new Date());
        if (report.getStatus() == null || report.getStatus().isEmpty()) {
            report.setStatus("0");
        }
        return rectReportMapper.insertRectReport(report);
    }

    @Override
    @Transactional
    public int updateRectReport(RectReport report) {
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setUpdateTime(new Date());
        return rectReportMapper.updateRectReport(report);
    }

    @Override
    public String generateReport(Long taskId) {
        StringBuilder sb = new StringBuilder();
        sb.append("【整改报告】\n\n一、整改方案\n");
        List<RectPlan> plans = rectPlanMapper.selectRectPlanByTaskId(taskId);
        if (plans != null && !plans.isEmpty()) {
            sb.append(plans.get(0).getPlanContent() != null ? plans.get(0).getPlanContent() : "待填报");
        } else {
            sb.append("待填报");
        }
        sb.append("\n\n二、佐证材料\n");
        // get issue id from task
        RectTask task = rectTaskMapper.selectRectTaskById(taskId);
        if (task != null && task.getIssueIds() != null) {
            String ids = task.getIssueIds().replace("[","").replace("]","");
            String[] arr = ids.split(",");
            if (arr.length > 0) {
                try {
                    Long iid = Long.parseLong(arr[0].trim());
                    List<RectMaterial> mats = rectMaterialMapper.selectRectMaterialByIssueId(iid);
                    if (mats != null && !mats.isEmpty()) {
                        for (RectMaterial m : mats) sb.append(m.getFileName()).append(" ");
                    } else { sb.append("无"); }
                } catch (Exception e) { sb.append("无"); }
            }
        }
        sb.append("\n\n三、整改成效\n已按方案完成整改措施，相关佐证材料已上传。");
        return sb.toString();
    }

    @Override
    @Transactional
    public int submitForApproval(Long reportId) {
        RectReport full = rectReportMapper.selectRectReportById(reportId);
        RectReport report = new RectReport();
        report.setReportId(reportId);
        report.setStatus("1");
        report.setUnitApproveStatus("0");
        report.setSubmitUserId(SecurityUtils.getUserId());
        report.setSubmitTime(new Date());
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setUpdateTime(new Date());
        int result = rectReportMapper.updateRectReport(report);
        if (full != null) {
            RectProgress p = new RectProgress();
            p.setTaskId(full.getTaskId());
            p.setProgressType("REPORT_SUBMIT");
            p.setContent("整改报告已提交，待单位负责人审批");
            p.setOperatorId(SecurityUtils.getUserId());
            p.setOperatorName(SecurityUtils.getUsername());
            p.setOperateTime(new Date());
            rectProgressMapper.insertRectProgress(p);
        }
        return result;
    }

    @Override
    @Transactional
    public int leaderApprove(Long reportId, String opinion, String status) {
        RectReport report = new RectReport();
        report.setReportId(reportId);
        report.setUnitApproveStatus(status);
        report.setUnitApproveOpinion(opinion);
        report.setUnitApproveUserId(SecurityUtils.getUserId());
        report.setUnitApproveTime(new Date());
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setUpdateTime(new Date());
        if ("1".equals(status)) {
            report.setStatus("2");
        } else {
            report.setStatus("0");
        }
        int result = rectReportMapper.updateRectReport(report);
        RectReport full = rectReportMapper.selectRectReportById(reportId);
        if (full != null) {
            if (!"1".equals(status)) {
                rollbackPendingClosureAfterReportReject(full, opinion);
            }
            RectProgress p = new RectProgress();
            p.setTaskId(full.getTaskId());
            p.setProgressType("1".equals(status) ? "LEADER_APPROVE" : "LEADER_REJECT");
            p.setContent("1".equals(status) ? "单位负责人已审批通过整改报告" : "单位负责人已驳回整改报告，驳回原因：" + (opinion != null ? opinion : ""));
            p.setOperatorId(SecurityUtils.getUserId());
            p.setOperatorName(SecurityUtils.getUsername());
            p.setOperateTime(new Date());
            rectProgressMapper.insertRectProgress(p);
        }
        return result;
    }

    private void rollbackPendingClosureAfterReportReject(RectReport report, String opinion) {
        RectClosure query = new RectClosure();
        query.setTaskId(report.getTaskId());
        query.setStatus("0");
        List<RectClosure> pendingClosures = rectClosureMapper.selectRectClosureList(query);
        Date now = new Date();
        Long firstIssueId = null;
        for (RectClosure pending : pendingClosures) {
            RectClosure closure = new RectClosure();
            closure.setClosureId(pending.getClosureId());
            closure.setStatus("2");
            closure.setAuditResult("2");
            closure.setAuditOpinion("单位负责人驳回整改报告，原销号申请自动退回。驳回意见：" + (opinion != null ? opinion : ""));
            closure.setReRectRequired("请整改责任人修改整改报告并重新发起销号申请。");
            closure.setAuditUserId(SecurityUtils.getUserId());
            closure.setAuditTime(now);
            closure.setUpdateBy(SecurityUtils.getUsername());
            closure.setUpdateTime(now);
            rectClosureMapper.updateRectClosure(closure);
            if (firstIssueId == null) {
                firstIssueId = pending.getIssueId();
            }
        }

        RectTask task = new RectTask();
        task.setTaskId(report.getTaskId());
        task.setStatus("1");
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(now);
        rectTaskMapper.updateRectTask(task);

        if (firstIssueId != null) {
            RectIssue issue = new RectIssue();
            issue.setIssueId(firstIssueId);
            issue.setStatus("1");
            issue.setUpdateBy(SecurityUtils.getUsername());
            issue.setUpdateTime(now);
            rectIssueMapper.updateRectIssue(issue);
        }
    }
}
