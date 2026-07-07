package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectPlan;

/**
 * 整改计划Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectPlanMapper {

    /**
     * 查询计划列表
     *
     * @param plan 计划对象
     * @return 计划集合
     */
    public List<RectPlan> selectRectPlanList(RectPlan plan);

    /**
     * 查询计划详情
     *
     * @param planId 计划ID
     * @return 计划对象
     */
    public RectPlan selectRectPlanById(Long planId);

    /**
     * 按任务ID查询计划
     *
     * @param taskId 任务ID
     * @return 计划对象
     */
    public RectPlan selectRectPlanByTaskId(Long taskId);

    /**
     * 新增计划
     *
     * @param plan 计划对象
     * @return 结果
     */
    public int insertRectPlan(RectPlan plan);

    /**
     * 修改计划
     *
     * @param plan 计划对象
     * @return 结果
     */
    public int updateRectPlan(RectPlan plan);

    /**
     * 批量删除计划
     *
     * @param planIds 需要删除的计划ID
     * @return 结果
     */
    public int deleteRectPlanByIds(Long[] planIds);
}
