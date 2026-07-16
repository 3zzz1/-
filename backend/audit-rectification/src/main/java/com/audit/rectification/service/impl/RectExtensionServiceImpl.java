package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectExtension;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.mapper.RectExtensionMapper;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectPlanMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.audit.rectification.service.IRectNotificationService;
import com.audit.rectification.service.IRectExtensionService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectExtensionServiceImpl implements IRectExtensionService {

    @Autowired
    private RectExtensionMapper rectExtensionMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;
    @Autowired
    private RectTaskMapper rectTaskMapper;
    @Autowired
    private RectPlanMapper rectPlanMapper;
    @Autowired
    private RectIssueMapper rectIssueMapper;
    @Autowired
    private IRectNotificationService rectNotificationService;

    @Override
    @Transactional
    public int insertRectExtension(RectExtension extension) {
        validateApplication(extension);
        RectExtension latest = rectExtensionMapper.selectLatestByTaskId(extension.getTaskId());
        if (latest != null && ("0".equals(latest.getStatus()) || "1".equals(latest.getStatus()))) {
            throw new ServiceException("该任务已有方案变更申请正在审批中");
        }
        extension.setApplyUserId(SecurityUtils.getUserId());
        extension.setApplyTime(new Date());
        extension.setStatus("0");
        extension.setCreateBy(SecurityUtils.getUsername());
        extension.setCreateTime(new Date());
        int result = rectExtensionMapper.insertRectExtension(extension);
        RectProgress p = new RectProgress();
        p.setTaskId(extension.getTaskId());
        p.setIssueId(extension.getIssueId());
        p.setProgressType("1".equals(extension.getApplyType()) ? "EXTENSION_APPLY" : "LONG_TERM_APPLY");
        p.setContent("1".equals(extension.getApplyType())
                ? "提交延期申请：" + extension.getOriginalDeadline() + " -> " + extension.getNewDeadline()
                : "提交长期持续整改申请：" + extension.getReason());
        p.setOperatorId(SecurityUtils.getUserId());
        p.setOperatorName(SecurityUtils.getUsername());
        p.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(p);
        RectTask task = rectTaskMapper.selectRectTaskById(extension.getTaskId());
        rectNotificationService.notifyRoles(new String[] { "audited_unit_leader" }, task.getRectDeptId(),
                extension.getTaskId(), extension.getIssueId(), "方案变更待审批",
                changeTypeName(extension) + "已提交，请及时审批。任务：" + task.getTaskNo());
        return result;
    }

    @Override
    @Transactional
    public int approveExtension(Long extensionId, String status, String opinion) {
        RectExtension existing = rectExtensionMapper.selectRectExtensionById(extensionId);
        if (existing == null) {
            throw new ServiceException("方案变更申请不存在");
        }
        if (!"1".equals(status) && !"2".equals(status)) {
            throw new ServiceException("审批结果不正确");
        }
        RectTask task = rectTaskMapper.selectRectTaskById(existing.getTaskId());
        boolean passed = "1".equals(status);
        RectExtension update = new RectExtension();
        update.setExtensionId(extensionId);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(new Date());

        String progressType;
        String progressContent;
        if ("0".equals(existing.getStatus())) {
            validateUnitApprover(task);
            update.setStatus(passed ? "1" : "3");
            update.setApproveUserId(SecurityUtils.getUserId());
            update.setApproveTime(new Date());
            update.setApproveOpinion(opinion);
            progressType = passed ? "CHANGE_UNIT_APPROVE" : "CHANGE_UNIT_REJECT";
            progressContent = passed ? "单位负责人已通过" + changeTypeName(existing) : "单位负责人已驳回" + changeTypeName(existing) + "：" + safe(opinion);
        } else if ("1".equals(existing.getStatus())) {
            validateAuditApprover();
            update.setStatus(passed ? "2" : "4");
            update.setAuditApproveUserId(SecurityUtils.getUserId());
            update.setAuditApproveTime(new Date());
            update.setAuditApproveOpinion(opinion);
            progressType = passed ? "CHANGE_AUDIT_APPROVE" : "CHANGE_AUDIT_REJECT";
            progressContent = passed ? "审计处已通过" + changeTypeName(existing) : "审计处已驳回" + changeTypeName(existing) + "：" + safe(opinion);
        } else {
            throw new ServiceException("该申请已完成审批");
        }

        int result = rectExtensionMapper.updateRectExtension(update);
        if (passed && "1".equals(existing.getStatus())) {
            applyApprovedChange(existing, task);
        }
        addProgress(existing, progressType, progressContent);
        if (passed && "0".equals(existing.getStatus())) {
            rectNotificationService.notifyRoles(new String[] { "admin", "audit_director", "audit_lead" }, null,
                    existing.getTaskId(), existing.getIssueId(), "方案变更待审计处审批",
                    changeTypeName(existing) + "已通过单位审批，请进行最终审批。任务：" + task.getTaskNo());
        } else {
            rectNotificationService.notifyUser(existing.getApplyUserId(), existing.getTaskId(), existing.getIssueId(),
                    passed ? "方案变更审批通过" : "方案变更申请被驳回", progressContent);
        }
        return result;
    }

    @Override
    public RectExtension selectLatestByTaskId(Long taskId) {
        return rectExtensionMapper.selectLatestByTaskId(taskId);
    }

    @Override
    public List<RectExtension> selectPendingList() {
        RectExtension query = new RectExtension();
        if (SecurityUtils.hasRole("audited_unit_leader")) {
            query.setStatus("0");
            List<RectExtension> result = new ArrayList<>();
            for (RectExtension item : rectExtensionMapper.selectRectExtensionList(query)) {
                RectTask task = rectTaskMapper.selectRectTaskById(item.getTaskId());
                if (task != null && Objects.equals(task.getRectDeptId(), SecurityUtils.getLoginUser().getDeptId())) {
                    result.add(item);
                }
            }
            return result;
        }
        validateAuditApprover();
        query.setStatus("1");
        return rectExtensionMapper.selectRectExtensionList(query);
    }

    private void validateApplication(RectExtension extension) {
        if (extension == null || extension.getTaskId() == null || extension.getIssueId() == null) {
            throw new ServiceException("任务和问题信息不能为空");
        }
        if (!"1".equals(extension.getApplyType()) && !"2".equals(extension.getApplyType())) {
            throw new ServiceException("申请类型不正确");
        }
        boolean responsible = rectPlanMapper.selectRectPlanByTaskId(extension.getTaskId()).stream()
                .anyMatch(plan -> Objects.equals(plan.getResponsibleUserId(), SecurityUtils.getUserId()));
        if (!responsible) {
            throw new ServiceException("仅该任务的整改责任人可以提交申请");
        }
        if (extension.getReason() == null || extension.getReason().trim().isEmpty()) {
            throw new ServiceException("申请原因不能为空");
        }
        if ("1".equals(extension.getApplyType()) && (extension.getOriginalDeadline() == null || extension.getNewDeadline() == null
                || !extension.getNewDeadline().after(extension.getOriginalDeadline()))) {
            throw new ServiceException("延期后的日期必须晚于原截止日期");
        }
        if ("2".equals(extension.getApplyType()) && (extension.getStageGoal() == null
                || extension.getReviewDate() == null || extension.getExpectedFinishDate() == null)) {
            throw new ServiceException("持续整改的阶段目标和日期不能为空");
        }
    }

    private void validateUnitApprover(RectTask task) {
        if (!SecurityUtils.hasRole("audited_unit_leader") || task == null
                || !Objects.equals(task.getRectDeptId(), SecurityUtils.getLoginUser().getDeptId())) {
            throw new ServiceException("仅本单位负责人可以审批");
        }
    }

    private void validateAuditApprover() {
        if (!SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("audit_director") && !SecurityUtils.hasRole("audit_lead")) {
            throw new ServiceException("仅审计处负责人可以审批");
        }
    }

    private void applyApprovedChange(RectExtension application, RectTask task) {
        List<RectPlan> plans = rectPlanMapper.selectRectPlanByTaskId(application.getTaskId());
        if ("1".equals(application.getApplyType())) {
            RectTask taskUpdate = new RectTask();
            taskUpdate.setTaskId(application.getTaskId());
            taskUpdate.setDeadline(application.getNewDeadline());
            taskUpdate.setUpdateBy(SecurityUtils.getUsername());
            taskUpdate.setUpdateTime(new Date());
            rectTaskMapper.updateRectTask(taskUpdate);
            for (RectPlan plan : plans) {
                if (Objects.equals(plan.getIssueId(), application.getIssueId())) {
                    RectPlan planUpdate = new RectPlan();
                    planUpdate.setPlanId(plan.getPlanId());
                    planUpdate.setPlanDeadline(application.getNewDeadline());
                    planUpdate.setUpdateBy(SecurityUtils.getUsername());
                    planUpdate.setUpdateTime(new Date());
                    rectPlanMapper.updateRectPlan(planUpdate);
                }
            }
            RectIssue issueUpdate = new RectIssue();
            issueUpdate.setIssueId(application.getIssueId());
            issueUpdate.setDeadline(application.getNewDeadline());
            issueUpdate.setUpdateBy(SecurityUtils.getUsername());
            issueUpdate.setUpdateTime(new Date());
            rectIssueMapper.updateRectIssue(issueUpdate);
            return;
        }
        for (RectPlan plan : plans) {
            if (Objects.equals(plan.getIssueId(), application.getIssueId())) {
                RectPlan planUpdate = new RectPlan();
                planUpdate.setPlanId(plan.getPlanId());
                planUpdate.setPlanType("2");
                planUpdate.setLongTermReason(application.getReason());
                planUpdate.setPlanDeadline(application.getExpectedFinishDate());
                planUpdate.setUpdateBy(SecurityUtils.getUsername());
                planUpdate.setUpdateTime(new Date());
                rectPlanMapper.updateRectPlan(planUpdate);
            }
        }
        RectIssue issue = new RectIssue();
        issue.setIssueId(application.getIssueId());
        issue.setStatus("4");
        issue.setUpdateBy(SecurityUtils.getUsername());
        issue.setUpdateTime(new Date());
        rectIssueMapper.updateRectIssue(issue);
    }

    private void addProgress(RectExtension application, String type, String content) {
        RectProgress progress = new RectProgress();
        progress.setTaskId(application.getTaskId());
        progress.setIssueId(application.getIssueId());
        progress.setProgressType(type);
        progress.setContent(content);
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);
    }

    private String changeTypeName(RectExtension application) {
        return "2".equals(application.getApplyType()) ? "长期持续整改申请" : "延期申请";
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
