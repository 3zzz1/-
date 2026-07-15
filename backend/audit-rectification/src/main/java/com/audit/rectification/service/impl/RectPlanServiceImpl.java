package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.mapper.RectPlanMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.audit.rectification.service.IRectNotificationService;
import com.audit.rectification.service.IRectPlanService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectPlanServiceImpl implements IRectPlanService {

    @Autowired
    private RectPlanMapper rectPlanMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;
    @Autowired
    private RectTaskMapper rectTaskMapper;
    @Autowired
    private IRectNotificationService rectNotificationService;

    @Override
    public RectPlan selectRectPlanByTaskId(Long taskId) {
        List<RectPlan> plans = rectPlanMapper.selectRectPlanByTaskId(taskId);
        return (plans != null && !plans.isEmpty()) ? plans.get(0) : null;
    }

    @Override
    @Transactional
    public int insertRectPlan(RectPlan plan) {
        plan.setCreateBy(SecurityUtils.getUsername());
        plan.setCreateTime(new Date());
        int result = rectPlanMapper.insertRectPlan(plan);
        notifyResponsibleUser(plan);
        if ("1".equals(plan.getStatus())) {
            RectProgress p = new RectProgress();
            p.setTaskId(plan.getTaskId());
            p.setIssueId(plan.getIssueId());
            p.setProgressType("PLAN_SUBMIT");
            p.setContent("整改方案已提交");
            p.setOperatorId(SecurityUtils.getUserId());
            p.setOperatorName(SecurityUtils.getUsername());
            p.setOperateTime(new Date());
            rectProgressMapper.insertRectProgress(p);
        }
        return result;
    }

    private void notifyResponsibleUser(RectPlan plan) {
        if (plan == null || plan.getResponsibleUserId() == null || plan.getTaskId() == null) {
            return;
        }
        RectTask task = rectTaskMapper.selectRectTaskById(plan.getTaskId());
        String taskNo = task != null && task.getTaskNo() != null ? task.getTaskNo() : "整改任务";
        rectNotificationService.notifyUser(plan.getResponsibleUserId(), plan.getTaskId(), plan.getIssueId(),
                "整改任务已分办", "联络员已将整改任务分办给你：" + taskNo
                        + "，请及时处理。可在我的任务中下载整改通知书。");

        RectProgress progress = new RectProgress();
        progress.setTaskId(plan.getTaskId());
        progress.setIssueId(plan.getIssueId());
        progress.setProgressType("ASSIGN");
        progress.setContent("整改任务已分办给责任人，用户ID：" + plan.getResponsibleUserId());
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);
    }

    @Override
    @Transactional
    public int updateRectPlan(RectPlan plan) {
        plan.setUpdateBy(SecurityUtils.getUsername());
        plan.setUpdateTime(new Date());
        int result = rectPlanMapper.updateRectPlan(plan);
        if ("1".equals(plan.getStatus())) {
            RectProgress p = new RectProgress();
            p.setTaskId(plan.getTaskId());
            p.setIssueId(plan.getIssueId());
            p.setProgressType("PLAN_SUBMIT");
            p.setContent("整改方案已修改并重新提交");
            p.setOperatorId(SecurityUtils.getUserId());
            p.setOperatorName(SecurityUtils.getUsername());
            p.setOperateTime(new Date());
            rectProgressMapper.insertRectProgress(p);
        }
        return result;
    }
}
