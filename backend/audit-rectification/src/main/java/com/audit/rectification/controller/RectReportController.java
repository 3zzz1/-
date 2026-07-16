package com.audit.rectification.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.RectReport;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.mapper.RectPlanMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.audit.rectification.service.IRectReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysUserService;

@RestController
@RequestMapping("/rectification/report")
public class RectReportController extends BaseController {

    @Autowired
    private IRectReportService rectReportService;

    @Autowired
    private RectPlanMapper rectPlanMapper;

    @Autowired
    private RectTaskMapper rectTaskMapper;

    @Autowired
    private ISysUserService sysUserService;

    @PreAuthorize("@ss.hasAnyPermi('rectification:report:query,rectification:report:add,rectification:report:generate,rectification:report:submit,rectification:report:approve') or @ss.hasAnyExactRoles('audited_unit_leader,school_leader')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getByTask(@PathVariable Long taskId) {
        RectReport report = rectReportService.selectRectReportByTaskId(taskId);
        if (SecurityUtils.hasExactRole("school_leader")
                && (report == null || !"1".equals(report.getUnitApproveStatus()))) {
            return success();
        }
        return success(report);
    }

    @PreAuthorize("@ss.hasExactRole('rect_responsible') and @ss.hasPermi('rectification:report:add')")
    @PostMapping
    public AjaxResult add(@RequestBody RectReport report) {
        return toAjax(rectReportService.insertRectReport(report));
    }

    @PreAuthorize("@ss.hasAnyPermi('rectification:report:generate,rectification:report:query,rectification:report:approve') or @ss.hasAnyExactRoles('audited_unit_leader,school_leader')")
    @GetMapping("/word/{taskId}")
    public void downloadReport(@PathVariable Long taskId, HttpServletResponse response) {
        try {
            RectReport report = rectReportService.selectRectReportByTaskId(taskId);
            if (SecurityUtils.hasExactRole("school_leader")
                    && (report == null || !"1".equals(report.getUnitApproveStatus()))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "仅可阅览单位审批通过的最终整改报告");
                return;
            }
            String content = rectReportService.generateReport(taskId);
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", buildContentDisposition(buildReportFileName(taskId)));

            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            run.setBold(true);
            run.setFontSize(18);
            run.setText("\u6574\u6539\u62a5\u544a");

            for (String line : content.split("\n")) {
                String text = line == null ? "" : line.trim();
                if (text.isEmpty()) {
                    continue;
                }
                paragraph = doc.createParagraph();
                run = paragraph.createRun();
                if (isReportSectionTitle(text)) {
                    run.setBold(true);
                    run.setFontSize(14);
                    run.setText(line);
                } else {
                    run.setFontSize(12);
                    run.setText(text);
                }
            }

            doc.write(response.getOutputStream());
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    private boolean isReportSectionTitle(String line) {
        if (line == null) {
            return false;
        }
        String text = line.trim();
        return text.startsWith("\u4e00\u3001")
                || text.startsWith("\u4e8c\u3001")
                || text.startsWith("\u4e09\u3001")
                || text.startsWith("\u56db\u3001");
    }

    private String buildReportFileName(Long taskId) {
        String personName = resolveResponsiblePersonName(taskId);
        if (personName == null || personName.trim().isEmpty()) {
            personName = "rectification_responsible";
        }
        return sanitizeFileName(personName.trim()) + "\u6574\u6539\u62a5\u544a.docx";
    }

    private String resolveCurrentUserName() {
        String userName = SecurityUtils.getUsername();
        if (userName == null || userName.trim().isEmpty()) {
            return null;
        }
        SysUser user = sysUserService.selectUserByUserName(userName);
        String displayName = displayName(user);
        return displayName != null && !displayName.trim().isEmpty() ? displayName : userName;
    }

    private String resolveResponsiblePersonName(Long taskId) {
        List<RectPlan> plans = rectPlanMapper.selectRectPlanByTaskId(taskId);
        if (plans != null && !plans.isEmpty()) {
            RectPlan plan = plans.get(0);
            if (plan.getResponsibleUserId() != null) {
                SysUser user = sysUserService.selectUserById(plan.getResponsibleUserId());
                String name = displayName(user);
                if (name != null && !name.trim().isEmpty()) {
                    return name;
                }
            }
            if (plan.getCreateBy() != null && !plan.getCreateBy().trim().isEmpty()) {
                return displayName(sysUserService.selectUserByUserName(plan.getCreateBy()));
            }
        }

        RectReport report = rectReportService.selectRectReportByTaskId(taskId);
        if (report != null && report.getCreateBy() != null && !report.getCreateBy().trim().isEmpty()) {
            String name = displayName(sysUserService.selectUserByUserName(report.getCreateBy()));
            return name != null ? name : report.getCreateBy();
        }

        RectTask task = rectTaskMapper.selectRectTaskById(taskId);
        return task != null ? task.getContactPerson() : null;
    }

    private String displayName(SysUser user) {
        if (user == null) {
            return null;
        }
        if (user.getNickName() != null && !user.getNickName().trim().isEmpty()) {
            return user.getNickName();
        }
        return user.getUserName();
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
    }

    private String buildContentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"report.docx\"; filename*=UTF-8''" + encoded;
    }

    @PreAuthorize("@ss.hasExactRole('rect_responsible') and @ss.hasPermi('rectification:report:generate')")
    @PostMapping("/generate/{taskId}")
    public AjaxResult generate(@PathVariable Long taskId) {
        return success(rectReportService.generateReport(taskId));
    }

    @PreAuthorize("@ss.hasExactRole('rect_responsible') and @ss.hasPermi('rectification:report:submit')")
    @PutMapping("/submit/{reportId}")
    public AjaxResult submit(@PathVariable Long reportId) {
        return toAjax(rectReportService.submitForApproval(reportId));
    }

    @PreAuthorize("@ss.hasExactRole('audited_unit_leader') and @ss.hasPermi('rectification:report:approve')")
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
