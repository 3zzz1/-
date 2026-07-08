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
    @GetMapping("/word/{taskId}")
    public void downloadReport(@PathVariable Long taskId, jakarta.servlet.http.HttpServletResponse response) {
        try {
            String content = rectReportService.generateReport(taskId);
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=report_" + taskId + ".docx");
            org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument();
            org.apache.poi.xwpf.usermodel.XWPFParagraph p;
            org.apache.poi.xwpf.usermodel.XWPFRun r;
            p = doc.createParagraph(); p.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            r = p.createRun(); r.setBold(true); r.setFontSize(18); r.setText("整改报告");
            for (String line : content.split("\n")) {
                if (line.startsWith("一、") || line.startsWith("二、") || line.startsWith("三、") || line.startsWith("四、")) {
                    p = doc.createParagraph();
                    r = p.createRun(); r.setBold(true); r.setFontSize(14); r.setText(line);
                } else if (line.trim().length() > 0) {
                    p = doc.createParagraph();
                    r = p.createRun(); r.setFontSize(12); r.setText(line.trim());
                }
            }
            doc.write(response.getOutputStream()); doc.close();
        } catch (Exception e) { throw new RuntimeException("Download failed", e); }
    }

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
