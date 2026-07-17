package com.audit.rectification.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;

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

    @Autowired
    private ISysUserService sysUserService;

    @PreAuthorize("@ss.hasExactRole('rect_responsible') and @ss.hasPermi('rectification:plan:query')")
    @GetMapping("/user-name/{userId}")
    public AjaxResult getUserName(@PathVariable Long userId) {
        SysUser user = sysUserService.selectUserById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("displayName", user == null ? ""
                : (user.getNickName() != null && !user.getNickName().trim().isEmpty()
                        ? user.getNickName() : user.getUserName()));
        return success(result);
    }

    /**
     * 根据任务ID查询整改方案
     */
    @PreAuthorize("@ss.hasExactRole('rect_responsible') and @ss.hasPermi('rectification:plan:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getByTask(@PathVariable Long taskId) {
        return success(rectPlanService.selectRectPlanByTaskId(taskId));
    }

    @PreAuthorize("@ss.hasAnyPermi('rectification:plan:query,rectification:plan:change:approve')")
    @GetMapping("/change/latest/{taskId}")
    public AjaxResult latestChange(@PathVariable Long taskId) {
        return success(rectExtensionService.selectLatestByTaskId(taskId));
    }

    @PreAuthorize("@ss.hasAnyExactRoles('audited_unit_leader,audit_director,audit_lead') and @ss.hasPermi('rectification:plan:change:approve')")
    @GetMapping("/change/pending")
    public AjaxResult pendingChanges() {
        List<RectExtension> list = rectExtensionService.selectPendingList();
        return success(list);
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
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : "0");
        plan.setRemark(dto.getRemark());
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
        extension.setApplyType("1");
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
    @PreAuthorize("@ss.hasAnyExactRoles('audited_unit_leader,audit_director,audit_lead') and @ss.hasPermi('rectification:plan:change:approve')")
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
    public AjaxResult applyLongTerm(@RequestBody ExtensionApplyDTO dto) {
        RectExtension extension = new RectExtension();
        extension.setApplyType("2");
        extension.setTaskId(dto.getTaskId());
        extension.setIssueId(dto.getIssueId());
        extension.setReason(dto.getReason());
        extension.setStageGoal(dto.getStageGoal());
        try {
            extension.setReviewDate(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getReviewDate()));
            extension.setExpectedFinishDate(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getExpectedFinishDate()));
        } catch (Exception e) {
            return error("日期格式错误");
        }
        return toAjax(rectExtensionService.insertRectExtension(extension));
    }
}
