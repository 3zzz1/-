package com.audit.rectification.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectReport;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectReportMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectReportService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectReportServiceImpl implements IRectReportService {

    @Autowired
    private RectReportMapper rectReportMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;

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
        int result = rectReportMapper.insertRectReport(report);
        if ("1".equals(report.getStatus())) {
            RectProgress p = new RectProgress();
            p.setTaskId(report.getTaskId());
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
    public int updateRectReport(RectReport report) {
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setUpdateTime(new Date());
        return rectReportMapper.updateRectReport(report);
    }

    @Override
    public String generateReport(Long taskId) {
        return "Report content will be generated via Apache POI";
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
