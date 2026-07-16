package com.audit.rectification.controller;

import java.util.List;
import java.util.HashMap;
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

import com.audit.rectification.domain.RectClosure;
import com.audit.rectification.domain.dto.ClosureReviewDTO;
import com.audit.rectification.service.IRectClosureService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * 审计整改销号Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/closure")
public class RectClosureController extends BaseController {

    @Autowired
    private IRectClosureService rectClosureService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * Return the display name needed by the closure workflow without exposing
     * the system user management API to business roles.
     */
    @PreAuthorize("@ss.hasAnyExactRoles('audit_director,audit_lead,audit_staff,school_leader') and @ss.hasAnyPermi('rectification:closure:list,rectification:closure:query,rectification:closure:audit')")
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
     * 查询销号列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:closure:list')")
    @GetMapping("/list")
    public TableDataInfo list(RectClosure closure) {
        if (SecurityUtils.hasExactRole("school_leader")) {
            closure.setStatus("1");
        }
        startPage();
        List<RectClosure> list = rectClosureService.selectRectClosureList(closure);
        return getDataTable(list);
    }

    /**
     * 获取销号详细信息
     */
    @PreAuthorize("@ss.hasPermi('rectification:closure:query')")
    @GetMapping(value = "/{closureId}")
    public AjaxResult getInfo(@PathVariable Long closureId) {
        RectClosure closure = rectClosureService.selectRectClosureById(closureId);
        if (SecurityUtils.hasExactRole("school_leader")
                && (closure == null || !"1".equals(closure.getStatus()))) {
            return error("仅可查看已销号的最终结果");
        }
        return success(closure);
    }

    /**
     * 获取任务最新销号申请状态
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:myList') or @ss.hasPermi('rectification:closure:list')")
    @GetMapping(value = "/task/{taskId}/latest")
    public AjaxResult getLatestByTaskId(@PathVariable Long taskId) {
        return success(rectClosureService.selectLatestRectClosureByTaskId(taskId));
    }

    /**
     * 申请销号
     */
    @PreAuthorize("@ss.hasPermi('rectification:closure:apply')")
    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody Map<String, Object> params) {
        Long issueId = params.get("issueId") != null
                ? Long.valueOf(params.get("issueId").toString()) : null;
        Long taskId = params.get("taskId") != null
                ? Long.valueOf(params.get("taskId").toString()) : null;
        String applyContent = params.get("applyContent") != null
                ? params.get("applyContent").toString() : null;
        return toAjax(rectClosureService.applyClosure(issueId, taskId, applyContent));
    }

    /**
     * 审核销号
     */
    @PreAuthorize("@ss.hasAnyExactRoles('audit_director,audit_lead') and @ss.hasPermi('rectification:closure:audit')")
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody ClosureReviewDTO dto) {
        return toAjax(rectClosureService.auditClosure(
                dto.getClosureId(), dto.getAuditResult(),
                dto.getAuditOpinion(), dto.getReRectRequired()));
    }
}
