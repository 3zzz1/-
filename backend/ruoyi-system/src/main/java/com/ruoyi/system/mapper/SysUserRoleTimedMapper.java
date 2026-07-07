package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysUserRoleTimed;

/**
 * 时效角色分配Mapper接口
 *
 * @author audit
 */
public interface SysUserRoleTimedMapper {

    /**
     * 查询时效角色分配列表
     */
    public List<SysUserRoleTimed> selectSysUserRoleTimedList(SysUserRoleTimed timed);

    /**
     * 按用户ID查询有效的时效角色
     */
    public List<SysUserRoleTimed> selectActiveByUserId(Long userId);

    /**
     * 查询所有已过期的有效记录
     */
    public List<SysUserRoleTimed> selectExpiredActiveRecords();

    /**
     * 新增时效角色分配
     */
    public int insertSysUserRoleTimed(SysUserRoleTimed timed);

    /**
     * 修改时效角色状态
     */
    public int updateSysUserRoleTimed(SysUserRoleTimed timed);

    /**
     * 按用户ID删除时效角色
     */
    public int deleteSysUserRoleTimedByUserId(Long userId);
}
