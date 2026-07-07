package com.audit.rectification.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectNotification;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.dto.TaskDispatchDTO;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectNotificationMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改任务Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectTaskServiceImpl implements IRectTaskService {

    @Autowired
    private RectTaskMapper rectTaskMapper;

    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Autowired
    private RectNotificationMapper rectNotificationMapper;

    @Override
    public List<RectTask> selectRectTaskList(RectTask task) {
        return rectTaskMapper.selectRectTaskList(task);
    }

    @Override
    public RectTask selectRectTaskById(Long taskId) {
        return rectTaskMapper.selectRectTaskById(taskId);
    }

    @Override
    @Transactional
    public int insertRectTask(TaskDispatchDTO dto) {
        // 生成任务编号: RW + yyyyMMddHHmmss
        String taskNo = "RW" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        RectTask task = new RectTask();
        task.setTaskNo(taskNo);
        // 将问题ID列表转为JSON数组文本
        if (dto.getIssueIds() != null && !dto.getIssueIds().isEmpty()) {
            task.setIssueIds(dto.getIssueIds().toString());
        }
        task.setRectDeptId(dto.getRectDeptId());
        task.setContactPerson(dto.getContactPerson());
        task.setContactPhone(dto.getContactPhone());
        task.setTaskRequirement(dto.getTaskRequirement());
        if (dto.getDeadline() != null) {
            try {
                task.setDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getDeadline()));
            } catch (Exception e) {
                // ignore parse error
            }
        }
        task.setStatus("0");
        task.setDispatchUserId(SecurityUtils.getUserId());
        task.setDispatchTime(new Date());
        task.setCreateBy(SecurityUtils.getUsername());
        task.setCreateTime(new Date());

        int rows = rectTaskMapper.insertRectTask(task);

        // 创建进度记录
        RectProgress progress = new RectProgress();
        progress.setTaskId(task.getTaskId());
        progress.setProgressType("DISPATCH");
        progress.setContent("任务下发，任务编号：" + taskNo);
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);

        // 发送通知给整改单位联系人
        RectNotification notification = new RectNotification();
        notification.setTaskId(task.getTaskId());
        notification.setNotifyType("SYSTEM_MSG");
        notification.setTitle("整改任务通知");
        notification.setContent("您收到一份整改任务（编号：" + taskNo + "），请及时确认并开展整改工作。");
        notification.setSendStatus("1");
        notification.setSendTime(new Date());
        notification.setReadStatus("0");
        notification.setCreateBy(SecurityUtils.getUsername());
        notification.setCreateTime(new Date());
        rectNotificationMapper.insertRectNotification(notification);

        return rows;
    }

    @Override
    @Transactional
    public int batchDispatch(List<TaskDispatchDTO> dtoList) {
        int totalRows = 0;
        for (TaskDispatchDTO dto : dtoList) {
            totalRows += insertRectTask(dto);
        }
        return totalRows;
    }

    @Override
    @Transactional
    public int confirmTask(Long taskId) {
        RectTask task = new RectTask();
        task.setTaskId(taskId);
        task.setStatus("1");
        task.setConfirmTime(new Date());
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(task);

        // 创建进度记录
        RectProgress progress = new RectProgress();
        progress.setTaskId(taskId);
        progress.setProgressType("CONFIRM");
        progress.setContent("任务已确认，开始整改");
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);

        return rows;
    }

    @Override
    @Transactional
    public int assignTask(Long taskId, Long assignUserId) {
        RectTask task = new RectTask();
        task.setTaskId(taskId);
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(task);

        // 创建进度记录
        RectProgress progress = new RectProgress();
        progress.setTaskId(taskId);
        progress.setProgressType("ASSIGN");
        progress.setContent("任务已指派给用户ID：" + assignUserId);
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);

        return rows;
    }

    @Override
    public String generateNotice(Long taskId) {
        // TODO: 使用Apache POI生成Word通知书，当前返回占位符
        RectTask task = selectRectTaskById(taskId);
        if (task == null) {
            return "任务不存在";
        }
        return "【整改通知书】\n"
                + "任务编号：" + task.getTaskNo() + "\n"
                + "联系人：" + task.getContactPerson() + "\n"
                + "联系电话：" + task.getContactPhone() + "\n"
                + "整改要求：" + task.getTaskRequirement() + "\n"
                + "截止日期：" + task.getDeadline() + "\n"
                + "（Word文档生成功能将通过Apache POI实现）";
    }

    @Override
    public List<RectTask> selectMyTaskList() {
        Long deptId = SecurityUtils.getLoginUser().getDeptId();
        return rectTaskMapper.selectRectTaskListByDeptId(deptId);
    }

    @Override
    public List<RectTask> selectOverdueAlertList() {
        Long deptId = SecurityUtils.getLoginUser().getDeptId();
        List<RectTask> allTasks = rectTaskMapper.selectRectTaskListByDeptId(deptId);
        List<RectTask> overdueTasks = new ArrayList<>();
        Date now = new Date();
        for (RectTask task : allTasks) {
            if (task.getDeadline() != null && task.getDeadline().before(now)
                    && !"4".equals(task.getStatus()) && !"3".equals(task.getStatus())) {
                overdueTasks.add(task);
            }
        }
        return overdueTasks;
    }

    @Override
    @Transactional
    public int updateRectTask(RectTask task) {
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        return rectTaskMapper.updateRectTask(task);
    }

    @Override
    @Transactional
    public int deleteRectTaskByIds(Long[] taskIds) {
        return rectTaskMapper.deleteRectTaskByIds(taskIds);
    }
}
