package com.audit.rectification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.service.IRectProgressService;
import com.audit.rectification.service.ExternalAuditorProjectScopeService;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 审计整改进度日志Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/progress")
public class RectProgressController extends BaseController {

    @Autowired
    private IRectProgressService rectProgressService;

    @Autowired
    private IRectTaskService rectTaskService;

    @Autowired
    private ExternalAuditorProjectScopeService externalProjectScope;

    /**
     * 根据任务ID查询整改进度日志
     */
    @PreAuthorize("@ss.hasPermi('rectification:progress:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getByTask(@PathVariable Long taskId) {
        externalProjectScope.checkTaskAccess(rectTaskService.selectRectTaskById(taskId));
        return success(rectProgressService.selectRectProgressByTaskId(taskId));
    }
}
