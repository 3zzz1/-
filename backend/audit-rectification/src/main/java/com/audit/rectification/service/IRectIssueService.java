package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectIssue;

/**
 * 审计整改问题Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectIssueService {

    /**
     * 查询问题列表
     *
     * @param issue 问题对象
     * @return 问题集合
     */
    List<RectIssue> selectRectIssueList(RectIssue issue);

    /**
     * 查询问题详情
     *
     * @param issueId 问题ID
     * @return 问题对象
     */
    RectIssue selectRectIssueById(Long issueId);

    /**
     * 新增问题
     *
     * @param issue 问题对象
     * @return 结果
     */
    int insertRectIssue(RectIssue issue);

    /**
     * 修改问题
     *
     * @param issue 问题对象
     * @return 结果
     */
    int updateRectIssue(RectIssue issue);

    /**
     * 批量删除问题
     *
     * @param issueIds 需要删除的问题ID
     * @return 结果
     */
    int deleteRectIssueByIds(Long[] issueIds);

    /**
     * 从审计项目同步问题
     *
     * @param projectId 项目ID
     * @return 结果
     */
    int syncFromProject(Long projectId);
}
