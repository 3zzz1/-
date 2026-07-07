package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectClosure;

/**
 * 销号申请Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectClosureMapper {

    /**
     * 查询销号申请列表
     *
     * @param closure 销号申请对象
     * @return 销号申请集合
     */
    public List<RectClosure> selectRectClosureList(RectClosure closure);

    /**
     * 查询销号申请详情
     *
     * @param closureId 销号申请ID
     * @return 销号申请对象
     */
    public RectClosure selectRectClosureById(Long closureId);

    /**
     * 按问题ID查询销号申请
     *
     * @param issueId 问题ID
     * @return 销号申请集合
     */
    public List<RectClosure> selectRectClosureByIssueId(Long issueId);

    /**
     * 新增销号申请
     *
     * @param closure 销号申请对象
     * @return 结果
     */
    public int insertRectClosure(RectClosure closure);

    /**
     * 修改销号申请
     *
     * @param closure 销号申请对象
     * @return 结果
     */
    public int updateRectClosure(RectClosure closure);

    /**
     * 批量删除销号申请
     *
     * @param closureIds 需要删除的销号申请ID
     * @return 结果
     */
    public int deleteRectClosureByIds(Long[] closureIds);
}
