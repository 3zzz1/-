package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectProgress;

/**
 * 审计整改进度Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectProgressService {

    /**
     * 按任务ID查询进度列表
     *
     * @param taskId 任务ID
     * @return 进度集合
     */
    List<RectProgress> selectRectProgressByTaskId(Long taskId);

    /**
     * 新增进度记录
     *
     * @param progress 进度对象
     * @return 结果
     */
    int insertRectProgress(RectProgress progress);
}
