package com.audit.rectification.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectReport;
import com.audit.rectification.service.IRectReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 审计整改报告Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/report")
public class RectReportController extends BaseController {

    @Autowired
    private IRectReportService rectReportService;

    /**
     * 根据任务ID查询整改报告
     */
    @PreAuthorize("@ss.hasPermi('rectification:report:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getByTask(@PathVariable Long taskId) {
        return success(rectReportService.selectRectReportByTaskId(taskId));
    }

    /**
     * 新增整改报告
     */
    @PreAuthorize("@ss.hasPermi('rectification:report:add')")
    @PostMapping
    public AjaxResult add(@RequestBody RectReport report) {
        return toAjax(rectReportService.insertRectReport(report));
    }

    /**
     * 生成整改报告
     */
    @PreAuthorize("@ss.hasPermi('rectification:report:generate')")
    @PostMapping("/generate/{taskId}")
    public AjaxResult generate(@PathVariable Long taskId) {
        return success(rectReportService.generateReport(taskId));
    }

    /**
     * 提交整改报告
     */
    @PreAuthorize("@ss.hasPermi('rectification:report:submit')")
    @PutMapping("/submit/{reportId}")
    public AjaxResult submit(@PathVariable Long reportId) {
        return toAjax(rectReportService.submitForApproval(reportId));
    }

    /**
     * 单位领导审批
     */
    @PreAuthorize("@ss.hasPermi('rectification:report:approve')")
    @PutMapping("/leader-approve")
    public AjaxResult leaderApprove(@RequestBody Map<String, Object> params) {
        Long reportId = params.get("reportId") != null
                ? Long.valueOf(params.get("reportId").toString()) : null;
        String approveResult = params.get("approveResult") != null
                ? params.get("approveResult").toString() : null;
        String approveOpinion = params.get("approveOpinion") != null
                ? params.get("approveOpinion").toString() : null;
        return toAjax(rectReportService.leaderApprove(reportId, approveOpinion, approveResult));
    }
}
