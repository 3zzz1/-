package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectProgress;

/**
 * 整改进度Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectProgressMapper {

    /**
     * 查询进度列表
     *
     * @param progress 进度对象
     * @return 进度集合
     */
    public List<RectProgress> selectRectProgressList(RectProgress progress);

    /**
     * 按任务ID查询进度列表
     *
     * @param taskId 任务ID
     * @return 进度集合
     */
    public List<RectProgress> selectRectProgressByTaskId(Long taskId);

    /**
     * 新增进度记录
     *
     * @param progress 进度对象
     * @return 结果
     */
    public int insertRectProgress(RectProgress progress);
}
