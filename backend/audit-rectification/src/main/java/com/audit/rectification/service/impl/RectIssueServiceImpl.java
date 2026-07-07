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
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改问题Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectIssueServiceImpl implements IRectIssueService {

    @Autowired
    private RectIssueMapper rectIssueMapper;

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
        return rectIssueMapper.insertRectIssue(issue);
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
        // TODO: 从审计项目自动同步问题
        return 0;
    }
}
