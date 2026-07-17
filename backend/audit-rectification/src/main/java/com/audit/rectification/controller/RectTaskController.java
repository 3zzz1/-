package com.audit.rectification.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.dto.TaskDispatchDTO;
import com.audit.rectification.mapper.RectPlanMapper;
import com.audit.rectification.service.ExternalAuditorProjectScopeService;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * 审计整改任务Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/task")
public class RectTaskController extends BaseController {

    @Autowired
    private IRectTaskService rectTaskService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RectPlanMapper planMapper;

    @Autowired
    private ExternalAuditorProjectScopeService externalProjectScope;

    @PreAuthorize("@ss.hasAnyExactRoles('audit_director,audit_lead') and @ss.hasAnyPermi('rectification:task:dispatch,rectification:task:batchDispatch')")
    @GetMapping("/liaisons")
    public AjaxResult liaisons(@RequestParam Long deptId) {
        List<SysUser> users = userMapper.selectUsersByRoleKeys(
                new String[] { "audited_unit_liaison" }, deptId);
        return success(users);
    }

    @PreAuthorize("@ss.hasExactRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:assign')")
    @GetMapping("/executors")
    public AjaxResult executors(@RequestParam Long deptId) {
        List<SysUser> users = userMapper.selectUsersByRoleKeys(
                new String[] { "rect_responsible" }, deptId);
        return success(users);
    }

    @PreAuthorize("@ss.hasExactRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:assign')")
    @GetMapping("/assignments/{taskId}")
    public AjaxResult assignments(@PathVariable Long taskId) {
        RectTask task = rectTaskService.selectRectTaskById(taskId);
        if (task == null || !Objects.equals(SecurityUtils.getDeptId(), task.getRectDeptId())) {
            return error("无权查看该任务的分办记录");
        }

        RectPlan query = new RectPlan();
        query.setTaskId(taskId);
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> assignedIssueIds = new HashSet<>();
        for (RectPlan plan : planMapper.selectRectPlanList(query)) {
            if (plan.getResponsibleUserId() == null || !assignedIssueIds.add(plan.getIssueId())) {
                continue;
            }
            SysUser user = userMapper.selectUserById(plan.getResponsibleUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("issueId", plan.getIssueId());
            item.put("responsibleUserId", plan.getResponsibleUserId());
            item.put("responsibleName", user == null ? "" : user.getNickName());
            item.put("responsibilityCategory", user == null ? "" : user.getRemark());
            item.put("assignTime", plan.getCreateTime());
            result.add(item);
        }
        return success(result);
    }

    /**
     * 查询整改任务列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(RectTask task) {
        externalProjectScope.applyTaskScope(task);
        startPage();
        List<RectTask> list = rectTaskService.selectRectTaskList(task);
        return getDataTable(list);
    }

    /**
     * 查询我的整改任务列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:myList')")
    @GetMapping("/my-list")
    public TableDataInfo myList() {
        startPage();
        List<RectTask> list = rectTaskService.selectMyTaskList();
        return getDataTable(list);
    }

    /**
     * 获取整改任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId) {
        RectTask task = rectTaskService.selectRectTaskById(taskId);
        externalProjectScope.checkTaskAccess(task);
        return success(task);
    }

    /**
     * 下发整改任务
     */
    @PreAuthorize("@ss.hasAnyExactRoles('audit_director,audit_lead') and @ss.hasPermi('rectification:task:dispatch')")
    @PostMapping
    public AjaxResult add(@RequestBody TaskDispatchDTO dto) {
        return toAjax(rectTaskService.insertRectTask(dto));
    }

    /**
     * 批量下发整改任务
     */
    @PreAuthorize("@ss.hasAnyExactRoles('audit_director,audit_lead') and @ss.hasPermi('rectification:task:batchDispatch')")
    @PostMapping("/batch")
    public AjaxResult batchDispatch(@RequestBody List<TaskDispatchDTO> dtoList) {
        return toAjax(rectTaskService.batchDispatch(dtoList));
    }

    /**
     * 确认整改任务
     */
    @PreAuthorize("@ss.hasExactRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:confirm')")
    @PutMapping("/confirm/{taskId}")
    public AjaxResult confirm(@PathVariable Long taskId) {
        return toAjax(rectTaskService.confirmTask(taskId));
    }

    /**
     * 指派整改任务
     */
    @PreAuthorize("@ss.hasExactRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:assign')")
    @PutMapping("/assign/{taskId}")
    public AjaxResult assign(@PathVariable Long taskId, @RequestBody Map<String, Long> params) {
        Long assigneeId = params.get("assigneeId");
        return toAjax(rectTaskService.assignTask(taskId, assigneeId));
    }

    /**
     * 生成整改通知书
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:notice') or @ss.hasPermi('rectification:task:query')")
    @PostMapping("/notice/{taskId}")
    public void generateNotice(@PathVariable Long taskId, HttpServletResponse response) {
        externalProjectScope.checkTaskAccess(rectTaskService.selectRectTaskById(taskId));
        rectTaskService.generateNotice(taskId, response);
    }

    /**
     * 逾期预警查询
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:alert')")
    @GetMapping("/overdue-alert")
    public AjaxResult overdueAlert() {
        return success(rectTaskService.selectOverdueAlertList());
    }

    /**
     * 修改整改任务
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody RectTask task) {
        return toAjax(rectTaskService.updateRectTask(task));
    }

    /**
     * 删除整改任务
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:remove')")
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(rectTaskService.deleteRectTaskByIds(taskIds));
    }
}
