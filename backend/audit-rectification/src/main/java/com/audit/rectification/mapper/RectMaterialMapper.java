package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectMaterial;

/**
 * 整改材料Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectMaterialMapper {

    /**
     * 查询材料列表
     *
     * @param material 材料对象
     * @return 材料集合
     */
    public List<RectMaterial> selectRectMaterialList(RectMaterial material);

    /**
     * 查询材料详情
     *
     * @param materialId 材料ID
     * @return 材料对象
     */
    public RectMaterial selectRectMaterialById(Long materialId);

    /**
     * 按问题ID查询材料
     *
     * @param issueId 问题ID
     * @return 材料集合
     */
    public List<RectMaterial> selectRectMaterialByIssueId(Long issueId);

    /**
     * 新增材料
     *
     * @param material 材料对象
     * @return 结果
     */
    public int insertRectMaterial(RectMaterial material);

    /**
     * 修改材料
     *
     * @param material 材料对象
     * @return 结果
     */
    public int updateRectMaterial(RectMaterial material);

    /**
     * 批量删除材料
     *
     * @param materialIds 需要删除的材料ID
     * @return 结果
     */
    public int deleteRectMaterialByIds(Long[] materialIds);
}
