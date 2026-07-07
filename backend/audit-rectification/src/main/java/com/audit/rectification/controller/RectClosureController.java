package com.audit.rectification.controller;

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

import com.audit.rectification.domain.RectClosure;
import com.audit.rectification.domain.dto.ClosureReviewDTO;
import com.audit.rectification.service.IRectClosureService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

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

    /**
     * 查询销号列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:closure:list')")
    @GetMapping("/list")
    public TableDataInfo list(RectClosure closure) {
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
        return success(rectClosureService.selectRectClosureById(closureId));
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
    @PreAuthorize("@ss.hasPermi('rectification:closure:audit')")
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody ClosureReviewDTO dto) {
        return toAjax(rectClosureService.auditClosure(
                dto.getClosureId(), dto.getAuditResult(),
                dto.getAuditOpinion(), dto.getReRectRequired()));
    }
}
