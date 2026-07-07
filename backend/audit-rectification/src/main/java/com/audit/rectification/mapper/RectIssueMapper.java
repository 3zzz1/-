package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectIssue;

/**
 * 审计整改问题Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectIssueMapper {

    /**
     * 查询问题列表
     *
     * @param issue 问题对象
     * @return 问题集合
     */
    public List<RectIssue> selectRectIssueList(RectIssue issue);

    /**
     * 查询问题详情
     *
     * @param issueId 问题ID
     * @return 问题对象
     */
    public RectIssue selectRectIssueById(Long issueId);

    /**
     * 新增问题
     *
     * @param issue 问题对象
     * @return 结果
     */
    public int insertRectIssue(RectIssue issue);

    /**
     * 修改问题
     *
     * @param issue 问题对象
     * @return 结果
     */
    public int updateRectIssue(RectIssue issue);

    /**
     * 批量删除问题
     *
     * @param issueIds 需要删除的问题ID
     * @return 结果
     */
    public int deleteRectIssueByIds(Long[] issueIds);
}
