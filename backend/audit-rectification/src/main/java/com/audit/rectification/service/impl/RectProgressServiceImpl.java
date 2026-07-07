package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectProgressService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 审计整改进度Service业务层实现
 *
 * @author audit
 * @date 2026-07-06
 */
@Service
public class RectProgressServiceImpl implements IRectProgressService {

    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Override
    public List<RectProgress> selectRectProgressByTaskId(Long taskId) {
        return rectProgressMapper.selectRectProgressByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertRectProgress(RectProgress progress) {
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        return rectProgressMapper.insertRectProgress(progress);
    }
}
