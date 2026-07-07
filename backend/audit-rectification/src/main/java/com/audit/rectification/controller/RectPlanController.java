package com.audit.rectification.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.audit.rectification.domain.RectExtension;
import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.dto.ExtensionApplyDTO;
import com.audit.rectification.domain.dto.PlanSubmitDTO;
import com.audit.rectification.service.IRectExtensionService;
import com.audit.rectification.service.IRectPlanService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 审计整改方案Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/plan")
public class RectPlanController extends BaseController {

    @Autowired
    private IRectPlanService rectPlanService;

    @Autowired
    private IRectExtensionService rectExtensionService;

    /**
     * 根据任务ID查询整改方案
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getByTask(@PathVariable Long taskId) {
        return success(rectPlanService.selectRectPlanByTaskId(taskId));
    }

    /**
     * 提交整改方案
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PlanSubmitDTO dto) {
        RectPlan plan = new RectPlan();
        plan.setTaskId(dto.getTaskId());
        plan.setIssueId(dto.getIssueId());
        plan.setPlanContent(dto.getPlanContent());
        plan.setResponsibleUserId(dto.getResponsibleUserId());
        plan.setPlanType(dto.getPlanType());
        plan.setLongTermReason(dto.getLongTermReason());
        try {
            plan.setPlanDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getPlanDeadline()));
        } catch (Exception e) {
            return error("日期格式错误");
        }
        return toAjax(rectPlanService.insertRectPlan(plan));
    }

    /**
     * 修改整改方案
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody RectPlan plan) {
        return toAjax(rectPlanService.updateRectPlan(plan));
    }

    /**
     * 申请延期
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:extension')")
    @PostMapping("/extension")
    public AjaxResult applyExtension(@RequestBody ExtensionApplyDTO dto) {
        RectExtension extension = new RectExtension();
        extension.setIssueId(dto.getIssueId());
        extension.setTaskId(dto.getTaskId());
        extension.setExtensionDays(dto.getExtensionDays());
        extension.setReason(dto.getReason());
        try {
            extension.setOriginalDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getOriginalDeadline()));
            extension.setNewDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getNewDeadline()));
        } catch (Exception e) {
            return error("日期格式错误");
        }
        extension.setApplyUserId(getUserId());
        extension.setApplyTime(new Date());
        extension.setStatus("0");
        return toAjax(rectExtensionService.insertRectExtension(extension));
    }

    /**
     * 审批延期申请
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:extension:approve')")
    @PutMapping("/extension/approve")
    public AjaxResult approveExtension(@RequestBody Map<String, Object> params) {
        Long extensionId = params.get("extensionId") != null
                ? Long.valueOf(params.get("extensionId").toString()) : null;
        String status = params.get("status") != null
                ? params.get("status").toString() : null;
        String opinion = params.get("opinion") != null
                ? params.get("opinion").toString() : null;
        return toAjax(rectExtensionService.approveExtension(extensionId, status, opinion));
    }

    /**
     * 申请持续整改
     */
    @PreAuthorize("@ss.hasPermi('rectification:plan:longTerm')")
    @PostMapping("/long-term")
    public AjaxResult applyLongTerm(@RequestBody Map<String, Object> params) {
        RectPlan plan = new RectPlan();
        Long taskId = params.get("taskId") != null
                ? Long.valueOf(params.get("taskId").toString()) : null;
        Long issueId = params.get("issueId") != null
                ? Long.valueOf(params.get("issueId").toString()) : null;
        String reason = params.get("reason") != null
                ? params.get("reason").toString() : null;
        plan.setTaskId(taskId);
        plan.setIssueId(issueId);
        plan.setPlanType("2");
        plan.setLongTermReason(reason);
        plan.setPlanContent(reason);
        return toAjax(rectPlanService.insertRectPlan(plan));
    }
}
