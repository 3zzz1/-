package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectTask;

/**
 * 整改任务Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectTaskMapper {

    /**
     * 查询任务列表
     *
     * @param task 任务对象
     * @return 任务集合
     */
    public List<RectTask> selectRectTaskList(RectTask task);

    /**
     * 查询任务详情
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    public RectTask selectRectTaskById(Long taskId);

    /**
     * 新增任务
     *
     * @param task 任务对象
     * @return 结果
     */
    public int insertRectTask(RectTask task);

    /**
     * 修改任务
     *
     * @param task 任务对象
     * @return 结果
     */
    public int updateRectTask(RectTask task);

    /**
     * 批量删除任务
     *
     * @param taskIds 需要删除的任务ID
     * @return 结果
     */
    public int deleteRectTaskByIds(Long[] taskIds);

    /**
     * 按部门ID查询任务列表
     *
     * @param deptId 部门ID
     * @return 任务集合
     */
    public List<RectTask> selectRectTaskListByDeptId(Long deptId);
}
