package com.audit.rectification.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.service.IRectIssueService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.service.ISysDeptService;

@RestController
@RequestMapping("/rectification/issue")
public class RectIssueController extends BaseController {

    @Autowired
    private IRectIssueService rectIssueService;

    @Autowired
    private ISysDeptService deptService;

    @GetMapping("/depts")
    public AjaxResult depts() {
        return success(deptService.selectDeptList(new SysDept()));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:list')")
    @GetMapping("/list")
    public TableDataInfo list(RectIssue issue) {
        startPage();
        List<RectIssue> list = rectIssueService.selectRectIssueList(issue);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:query')")
    @GetMapping(value = "/{issueId}")
    public AjaxResult getInfo(@PathVariable Long issueId) {
        return success(rectIssueService.selectRectIssueById(issueId));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:add')")
    @PostMapping
    public AjaxResult add(@RequestBody RectIssue issue) {
        return toAjax(rectIssueService.insertRectIssue(issue));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody RectIssue issue) {
        return toAjax(rectIssueService.updateRectIssue(issue));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:remove')")
    @DeleteMapping("/{issueIds}")
    public AjaxResult remove(@PathVariable Long[] issueIds) {
        return toAjax(rectIssueService.deleteRectIssueByIds(issueIds));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:sync')")
    @PostMapping("/sync/{projectId}")
    public AjaxResult sync(@PathVariable Long projectId) {
        return toAjax(rectIssueService.syncFromProject(projectId));
    }

    @PreAuthorize("@ss.hasPermi('rectification:issue:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        List<RectIssue> list = rectIssueService.selectRectIssueList(new RectIssue());
        ExcelUtil<RectIssue> util = new ExcelUtil<>(RectIssue.class);
        util.exportExcel(response, list, "问题台账");
    }
}
