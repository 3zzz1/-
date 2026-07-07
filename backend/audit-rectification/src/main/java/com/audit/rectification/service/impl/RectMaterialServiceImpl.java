package com.audit.rectification.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectMaterial;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.mapper.RectMaterialMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.service.IRectMaterialService;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class RectMaterialServiceImpl implements IRectMaterialService {

    @Autowired
    private RectMaterialMapper rectMaterialMapper;
    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Override
    public List<RectMaterial> selectRectMaterialByIssueId(Long issueId) {
        return rectMaterialMapper.selectRectMaterialByIssueId(issueId);
    }

    @Override
    @Transactional
    public int insertRectMaterial(RectMaterial material) {
        material.setUploadTime(new Date());
        material.setUploadUserId(SecurityUtils.getUserId());
        material.setCreateBy(SecurityUtils.getUsername());
        material.setCreateTime(new Date());
        int result = rectMaterialMapper.insertRectMaterial(material);
        RectProgress p = new RectProgress();
        p.setTaskId(material.getTaskId());
        p.setIssueId(material.getIssueId());
        p.setProgressType("MATERIAL_UPLOAD");
        p.setContent("Upload evidence: " + material.getFileName());
        p.setOperatorId(SecurityUtils.getUserId());
        p.setOperatorName(SecurityUtils.getUsername());
        p.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(p);
        return result;
    }

    @Override
    @Transactional
    public int deleteRectMaterialByIds(Long[] materialIds) {
        return rectMaterialMapper.deleteRectMaterialByIds(materialIds);
    }
}
