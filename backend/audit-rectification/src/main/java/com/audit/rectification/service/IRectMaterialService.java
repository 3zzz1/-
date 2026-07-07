package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectMaterial;

/**
 * 审计整改材料Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectMaterialService {

    /**
     * 按问题ID查询整改材料列表
     *
     * @param issueId 问题ID
     * @return 材料集合
     */
    List<RectMaterial> selectRectMaterialByIssueId(Long issueId);

    /**
     * 新增整改材料
     *
     * @param material 材料对象
     * @return 结果
     */
    int insertRectMaterial(RectMaterial material);

    /**
     * 批量删除整改材料
     *
     * @param materialIds 需要删除的材料ID
     * @return 结果
     */
    int deleteRectMaterialByIds(Long[] materialIds);
}
