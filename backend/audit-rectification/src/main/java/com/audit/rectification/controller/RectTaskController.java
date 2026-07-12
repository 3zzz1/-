package com.audit.rectification.controller;

import java.util.List;
import java.util.Map;
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

import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.dto.TaskDispatchDTO;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

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

    /**
     * 查询整改任务列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(RectTask task) {
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
        return success(rectTaskService.selectRectTaskById(taskId));
    }

    /**
     * 下发整改任务
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:add')")
    @PostMapping
    public AjaxResult add(@RequestBody TaskDispatchDTO dto) {
        return toAjax(rectTaskService.insertRectTask(dto));
    }

    /**
     * 批量下发整改任务
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:add')")
    @PostMapping("/batch")
    public AjaxResult batchDispatch(@RequestBody List<TaskDispatchDTO> dtoList) {
        return toAjax(rectTaskService.batchDispatch(dtoList));
    }

    /**
     * 确认整改任务
     */
    @PreAuthorize("@ss.hasRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:confirm')")
    @PutMapping("/confirm/{taskId}")
    public AjaxResult confirm(@PathVariable Long taskId) {
        return toAjax(rectTaskService.confirmTask(taskId));
    }

    /**
     * 指派整改任务
     */
    @PreAuthorize("@ss.hasRole('audited_unit_liaison') and @ss.hasPermi('rectification:task:assign')")
    @PutMapping("/assign/{taskId}")
    public AjaxResult assign(@PathVariable Long taskId, @RequestBody Map<String, Long> params) {
        Long assigneeId = params.get("assigneeId");
        return toAjax(rectTaskService.assignTask(taskId, assigneeId));
    }

    /**
     * 生成整改通知书
     */
    @PreAuthorize("@ss.hasPermi('rectification:task:notice')")
    @PostMapping("/notice/{taskId}")
    public void generateNotice(@PathVariable Long taskId, HttpServletResponse response) {
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
