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

    @Override
    public RectReport selectRectReportByTaskId(Long taskId) {
        return rectReportMapper.selectRectReportByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertRectReport(RectReport report) {
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
        report.setSubmitUserId(SecurityUtils.getUserId());
        report.setSubmitTime(new Date());
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setUpdateTime(new Date());
        int result = rectReportMapper.updateRectReport(report);
        if (full != null) {
            RectProgress p = new RectProgress();
            p.setTaskId(full.getTaskId());
            p.setProgressType("REPORT_SUBMIT");
            p.setContent("Submit rectification report");
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
            report.setStatus("1");
        }
        int result = rectReportMapper.updateRectReport(report);
        RectReport full = rectReportMapper.selectRectReportById(reportId);
        if (full != null) {
            RectProgress p = new RectProgress();
            p.setTaskId(full.getTaskId());
            p.setProgressType("1".equals(status) ? "LEADER_APPROVE" : "LEADER_REJECT");
            p.setContent("1".equals(status) ? "Leader approved" : "Leader rejected: " + (opinion != null ? opinion : ""));
            p.setOperatorId(SecurityUtils.getUserId());
            p.setOperatorName(SecurityUtils.getUsername());
            p.setOperateTime(new Date());
            rectProgressMapper.insertRectProgress(p);
        }
        return result;
    }
}
