package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectClosure;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectClosureMapper;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectClosureService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改销号Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectClosureServiceImpl implements IRectClosureService {

    @Autowired
    private RectClosureMapper rectClosureMapper;

    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Override
    public List<RectClosure> selectRectClosureList(RectClosure closure) {
        return rectClosureMapper.selectRectClosureList(closure);
    }

    @Override
    public RectClosure selectRectClosureById(Long closureId) {
        return rectClosureMapper.selectRectClosureById(closureId);
    }

    @Override
    @Transactional
    public int applyClosure(Long issueId, Long taskId, String content) {
        // 创建销号记录
        RectClosure closure = new RectClosure();
        closure.setIssueId(issueId);
        closure.setTaskId(taskId);
        closure.setApplyContent(content);
        closure.setStatus("0");
        closure.setApplyUserId(SecurityUtils.getUserId());
        closure.setApplyTime(new Date());
        closure.setCreateBy(SecurityUtils.getUsername());
        closure.setCreateTime(new Date());
        int rows = rectClosureMapper.insertRectClosure(closure);

        // 更新问题状态为"待审核"
        RectIssue issue = new RectIssue();
        issue.setIssueId(issueId);
        issue.setStatus("2");
        issue.setUpdateBy(SecurityUtils.getUsername());
        issue.setUpdateTime(new Date());
        rectIssueMapper.updateRectIssue(issue);

        // 创建进度记录
        RectProgress progress = new RectProgress();
        progress.setTaskId(taskId);
        progress.setIssueId(issueId);
        progress.setProgressType("CLOSURE_APPLY");
        progress.setContent("申请销号：" + content);
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
    public int auditClosure(Long closureId, String result, String opinion, String reRectRequired) {
        // 查询销号记录
        RectClosure existingClosure = rectClosureMapper.selectRectClosureById(closureId);

        // 更新销号记录
        RectClosure closure = new RectClosure();
        closure.setClosureId(closureId);
        closure.setAuditResult(result);
        closure.setAuditOpinion(opinion);
        closure.setAuditUserId(SecurityUtils.getUserId());
        closure.setAuditTime(new Date());
        closure.setUpdateBy(SecurityUtils.getUsername());
        closure.setUpdateTime(new Date());

        if ("1".equals(result)) {
            // 审批通过：销号
            closure.setStatus("1");

            // 更新问题状态为"已销号"，设置销号日期
            RectIssue issue = new RectIssue();
            issue.setIssueId(existingClosure.getIssueId());
            issue.setStatus("3");
            issue.setClosureDate(new Date());
            issue.setUpdateBy(SecurityUtils.getUsername());
            issue.setUpdateTime(new Date());
            rectIssueMapper.updateRectIssue(issue);

            // 创建进度记录
            RectProgress progress = new RectProgress();
            progress.setTaskId(existingClosure.getTaskId());
            progress.setIssueId(existingClosure.getIssueId());
            progress.setProgressType("CLOSURE_APPROVED");
            progress.setContent("销号审核通过，问题已销号。审核意见：" + opinion);
            progress.setOperatorId(SecurityUtils.getUserId());
            progress.setOperatorName(SecurityUtils.getUsername());
            progress.setOperateTime(new Date());
            progress.setCreateBy(SecurityUtils.getUsername());
            progress.setCreateTime(new Date());
            rectProgressMapper.insertRectProgress(progress);
        } else {
            // 审批驳回：退回整改
            closure.setStatus("2");
            closure.setReRectRequired(reRectRequired);

            // 更新问题状态为"整改中"
            RectIssue issue = new RectIssue();
            issue.setIssueId(existingClosure.getIssueId());
            issue.setStatus("1");
            issue.setUpdateBy(SecurityUtils.getUsername());
            issue.setUpdateTime(new Date());
            rectIssueMapper.updateRectIssue(issue);

            // 创建进度记录
            RectProgress progress = new RectProgress();
            progress.setTaskId(existingClosure.getTaskId());
            progress.setIssueId(existingClosure.getIssueId());
            progress.setProgressType("CLOSURE_REJECTED");
            progress.setContent("销号审核驳回，需重新整改。审核意见：" + opinion
                    + "，补充要求：" + (reRectRequired != null ? reRectRequired : ""));
            progress.setOperatorId(SecurityUtils.getUserId());
            progress.setOperatorName(SecurityUtils.getUsername());
            progress.setOperateTime(new Date());
            progress.setCreateBy(SecurityUtils.getUsername());
            progress.setCreateTime(new Date());
            rectProgressMapper.insertRectProgress(progress);
        }

        return rectClosureMapper.updateRectClosure(closure);
    }
}
