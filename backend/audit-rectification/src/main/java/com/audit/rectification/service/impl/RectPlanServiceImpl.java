package com.audit.rectification.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectPlan;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectPlanMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectPlanService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectPlanServiceImpl implements IRectPlanService {

    @Autowired
    private RectPlanMapper rectPlanMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Override
    public RectPlan selectRectPlanByTaskId(Long taskId) {
        return rectPlanMapper.selectRectPlanByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertRectPlan(RectPlan plan) {
        plan.setCreateBy(SecurityUtils.getUsername());
        plan.setCreateTime(new Date());
        int result = rectPlanMapper.insertRectPlan(plan);
        RectProgress p = new RectProgress();
        p.setTaskId(plan.getTaskId());
        p.setIssueId(plan.getIssueId());
        p.setProgressType("PLAN_SUBMIT");
        p.setContent("提交整改方案" + ("2".equals(plan.getPlanType()) ? "(长期持续整改)" : ""));
        p.setOperatorId(SecurityUtils.getUserId());
        p.setOperatorName(SecurityUtils.getUsername());
        p.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(p);
        return result;
    }

    @Override
    @Transactional
    public int updateRectPlan(RectPlan plan) {
        plan.setUpdateBy(SecurityUtils.getUsername());
        plan.setUpdateTime(new Date());
        return rectPlanMapper.updateRectPlan(plan);
    }
}
