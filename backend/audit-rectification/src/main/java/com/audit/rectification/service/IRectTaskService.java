package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.dto.TaskDispatchDTO;

/**
 * 整改任务Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectTaskService {

    /**
     * 查询任务列表
     *
     * @param task 任务对象
     * @return 任务集合
     */
    List<RectTask> selectRectTaskList(RectTask task);

    /**
     * 查询任务详情
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    RectTask selectRectTaskById(Long taskId);

    /**
     * 新增整改任务（单个下发）
     *
     * @param dto 任务下发DTO
     * @return 结果
     */
    int insertRectTask(TaskDispatchDTO dto);

    /**
     * 批量下发整改任务
     *
     * @param dtoList 任务下发DTO列表
     * @return 结果
     */
    int batchDispatch(List<TaskDispatchDTO> dtoList);

    /**
     * 确认任务
     *
     * @param taskId 任务ID
     * @return 结果
     */
    int confirmTask(Long taskId);

    /**
     * 指派任务给指定用户
     *
     * @param taskId 任务ID
     * @param assignUserId 被指派人用户ID
     * @return 结果
     */
    int assignTask(Long taskId, Long assignUserId);

    /**
     * 生成整改通知书
     *
     * @param taskId 任务ID
     * @return 通知书内容
     */
    void generateNotice(Long taskId, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 查询我的任务列表
     *
     * @return 任务集合
     */
    List<RectTask> selectMyTaskList();

    /**
     * 查询逾期预警任务列表
     *
     * @return 任务集合
     */
    List<RectTask> selectOverdueAlertList();

    /**
     * 修改任务
     *
     * @param task 任务对象
     * @return 结果
     */
    int updateRectTask(RectTask task);

    /**
     * 批量删除任务
     *
     * @param taskIds 需要删除的任务ID
     * @return 结果
     */
    int deleteRectTaskByIds(Long[] taskIds);
}
