package com.audit.rectification.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.service.IRectIssueService;
import com.audit.rectification.service.IRectNotificationService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改问题 Service 实现。
 */
@Service
public class RectIssueServiceImpl implements IRectIssueService {

    private static final String[] AUDIT_NOTICE_ROLES = { "audit_director", "audit_lead" };

    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Autowired
    private IRectNotificationService rectNotificationService;

    @Override
    public List<RectIssue> selectRectIssueList(RectIssue issue) {
        return rectIssueMapper.selectRectIssueList(issue);
    }

    @Override
    public RectIssue selectRectIssueById(Long issueId) {
        return rectIssueMapper.selectRectIssueById(issueId);
    }

    @Override
    @Transactional
    public int insertRectIssue(RectIssue issue) {
        if (issue.getIssueNo() == null || issue.getIssueNo().isEmpty()) {
            issue.setIssueNo("ISS" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        }
        if (issue.getStatus() == null || issue.getStatus().isEmpty()) {
            issue.setStatus("0");
        }
        issue.setCreateBy(SecurityUtils.getUsername());
        issue.setCreateTime(new Date());
        int rows = rectIssueMapper.insertRectIssue(issue);
        if (rows > 0) {
            rectNotificationService.notifyRoles(AUDIT_NOTICE_ROLES, null, null, issue.getIssueId(),
                    "问题待下发整改任务",
                    "问题台账新增待下发问题：" + safeIssueTitle(issue) + "，请及时下发整改任务。");
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateRectIssue(RectIssue issue) {
        issue.setUpdateBy(SecurityUtils.getUsername());
        issue.setUpdateTime(new Date());
        return rectIssueMapper.updateRectIssue(issue);
    }

    @Override
    @Transactional
    public int deleteRectIssueByIds(Long[] issueIds) {
        return rectIssueMapper.deleteRectIssueByIds(issueIds);
    }

    @Override
    @Transactional
    public int syncFromProject(Long projectId) {
        return 0;
    }

    private String safeIssueTitle(RectIssue issue) {
        if (issue == null) {
            return "问题";
        }
        if (issue.getIssueTitle() != null && !issue.getIssueTitle().isEmpty()) {
            return issue.getIssueTitle();
        }
        return issue.getIssueNo() != null && !issue.getIssueNo().isEmpty() ? issue.getIssueNo() : "问题";
    }
}
