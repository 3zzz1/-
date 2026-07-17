package com.audit.rectification.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.mapper.RectIssueMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysUserRoleTimed;
import com.ruoyi.system.mapper.SysUserRoleTimedMapper;

/** 中介审计人员在整改模块中的项目数据范围。 */
@Service
public class ExternalAuditorProjectScopeService {

    private static final String PROJECT_SCOPE_PARAM = "externalProjectIds";

    @Autowired
    private SysUserRoleTimedMapper timedRoleMapper;

    @Autowired
    private RectIssueMapper issueMapper;

    public boolean isExternalAuditor() {
        return SecurityUtils.hasExactRole("external_auditor");
    }

    public void applyIssueScope(RectIssue issue) {
        if (isExternalAuditor()) {
            issue.getParams().put(PROJECT_SCOPE_PARAM, activeProjectIdsOrDenied());
        }
    }

    public void applyTaskScope(RectTask task) {
        if (isExternalAuditor()) {
            task.getParams().put(PROJECT_SCOPE_PARAM, activeProjectIdsOrDenied());
        }
    }

    public void checkIssueAccess(RectIssue issue) {
        if (isExternalAuditor() && (issue == null || !activeProjectIds().contains(issue.getSourceProjectId()))) {
            throw new ServiceException("无权查看非授权项目的问题");
        }
    }

    public void checkTaskAccess(RectTask task) {
        if (!isExternalAuditor()) {
            return;
        }
        if (task == null || task.getIssueIds() == null || task.getIssueIds().trim().isEmpty()) {
            throw new ServiceException("无权查看非授权项目的整改任务");
        }
        List<Long> projectIds = activeProjectIds();
        try {
            List<Long> issueIds = JSON.parseArray(task.getIssueIds(), Long.class);
            boolean allowed = issueIds.stream()
                    .map(issueMapper::selectRectIssueById)
                    .filter(Objects::nonNull)
                    .anyMatch(issue -> projectIds.contains(issue.getSourceProjectId()));
            if (allowed) {
                return;
            }
        } catch (RuntimeException ignored) {
            // 非法历史数据按无权限处理。
        }
        throw new ServiceException("无权查看非授权项目的整改任务");
    }

    private List<Long> activeProjectIdsOrDenied() {
        List<Long> projectIds = activeProjectIds();
        return projectIds.isEmpty() ? Collections.singletonList(-1L) : projectIds;
    }

    private List<Long> activeProjectIds() {
        return timedRoleMapper.selectActiveByUserId(SecurityUtils.getUserId()).stream()
                .map(SysUserRoleTimed::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
