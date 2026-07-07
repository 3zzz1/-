package com.audit.rectification.service;

import com.audit.rectification.domain.RectPlan;

/**
 * 审计整改方案Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectPlanService {

    /**
     * 按任务ID查询整改方案
     *
     * @param taskId 任务ID
     * @return 方案对象
     */
    RectPlan selectRectPlanByTaskId(Long taskId);

    /**
     * 新增整改方案
     *
     * @param plan 方案对象
     * @return 结果
     */
    int insertRectPlan(RectPlan plan);

    /**
     * 修改整改方案
     *
     * @param plan 方案对象
     * @return 结果
     */
    int updateRectPlan(RectPlan plan);
}
