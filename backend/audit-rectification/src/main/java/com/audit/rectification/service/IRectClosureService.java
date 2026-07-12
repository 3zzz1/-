package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectClosure;

/**
 * 审计整改销号Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectClosureService {

    /**
     * 查询销号申请列表
     *
     * @param closure 销号申请对象
     * @return 销号申请集合
     */
    List<RectClosure> selectRectClosureList(RectClosure closure);

    /**
     * 查询销号申请详情
     *
     * @param closureId 销号申请ID
     * @return 销号申请对象
     */
    RectClosure selectRectClosureById(Long closureId);

    /**
     * 按任务ID查询最新销号申请
     *
     * @param taskId 任务ID
     * @return 最新销号申请
     */
    RectClosure selectLatestRectClosureByTaskId(Long taskId);

    /**
     * 申请销号
     *
     * @param issueId 问题ID
     * @param taskId 任务ID
     * @param content 申请内容
     * @return 结果
     */
    int applyClosure(Long issueId, Long taskId, String content);

    /**
     * 审核销号
     *
     * @param closureId 销号申请ID
     * @param result 审核结果
     * @param opinion 审核意见
     * @param reRectRequired 是否需要重新整改
     * @return 结果
     */
    int auditClosure(Long closureId, String result, String opinion, String reRectRequired);
}
