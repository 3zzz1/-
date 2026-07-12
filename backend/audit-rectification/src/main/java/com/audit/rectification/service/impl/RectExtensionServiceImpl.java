package com.audit.rectification.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectExtension;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectExtensionMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectExtensionService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectExtensionServiceImpl implements IRectExtensionService {

    @Autowired
    private RectExtensionMapper rectExtensionMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Override
    @Transactional
    public int insertRectExtension(RectExtension extension) {
        extension.setApplyUserId(SecurityUtils.getUserId());
        extension.setApplyTime(new Date());
        extension.setStatus("0");
        extension.setCreateBy(SecurityUtils.getUsername());
        extension.setCreateTime(new Date());
        int result = rectExtensionMapper.insertRectExtension(extension);
        RectProgress p = new RectProgress();
        p.setTaskId(extension.getTaskId());
        p.setIssueId(extension.getIssueId());
        p.setProgressType("EXTENSION_APPLY");
        p.setContent("申请延期：" + extension.getOriginalDeadline() + " -> " + extension.getNewDeadline());
        p.setOperatorId(SecurityUtils.getUserId());
        p.setOperatorName(SecurityUtils.getUsername());
        p.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(p);
        return result;
    }

    @Override
    @Transactional
    public int approveExtension(Long extensionId, String status, String opinion) {
        RectExtension extension = new RectExtension();
        extension.setExtensionId(extensionId);
        extension.setStatus(status);
        extension.setApproveOpinion(opinion);
        extension.setApproveUserId(SecurityUtils.getUserId());
        extension.setApproveTime(new Date());
        extension.setUpdateBy(SecurityUtils.getUsername());
        extension.setUpdateTime(new Date());
        int result = rectExtensionMapper.updateRectExtension(extension);
        RectProgress p = new RectProgress();
        p.setProgressType("1".equals(status) ? "EXTENSION_APPROVE" : "EXTENSION_REJECT");
        p.setContent("1".equals(status) ? "延期申请已审批通过" : "延期申请已驳回，审批意见：" + (opinion != null ? opinion : ""));
        p.setOperatorId(SecurityUtils.getUserId());
        p.setOperatorName(SecurityUtils.getUsername());
        p.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(p);
        return result;
    }
}
