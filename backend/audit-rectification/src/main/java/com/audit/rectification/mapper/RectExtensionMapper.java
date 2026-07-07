package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectExtension;

/**
 * 延期申请Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectExtensionMapper {

    /**
     * 查询延期申请列表
     *
     * @param extension 延期申请对象
     * @return 延期申请集合
     */
    public List<RectExtension> selectRectExtensionList(RectExtension extension);

    /**
     * 查询延期申请详情
     *
     * @param extensionId 延期申请ID
     * @return 延期申请对象
     */
    public RectExtension selectRectExtensionById(Long extensionId);

    /**
     * 新增延期申请
     *
     * @param extension 延期申请对象
     * @return 结果
     */
    public int insertRectExtension(RectExtension extension);

    /**
     * 修改延期申请
     *
     * @param extension 延期申请对象
     * @return 结果
     */
    public int updateRectExtension(RectExtension extension);

    /**
     * 批量删除延期申请
     *
     * @param extensionIds 需要删除的延期申请ID
     * @return 结果
     */
    public int deleteRectExtensionByIds(Long[] extensionIds);
}
