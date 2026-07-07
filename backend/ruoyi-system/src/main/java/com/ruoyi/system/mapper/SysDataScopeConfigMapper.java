package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDataScopeConfig;

/**
 * 数据权限范围配置Mapper接口
 *
 * @author audit
 */
public interface SysDataScopeConfigMapper {

    /**
     * 查询数据权限配置列表
     */
    public List<SysDataScopeConfig> selectSysDataScopeConfigList(SysDataScopeConfig config);

    /**
     * 按角色ID查询数据权限配置
     */
    public List<SysDataScopeConfig> selectSysDataScopeConfigByRoleId(Long roleId);

    /**
     * 新增数据权限配置
     */
    public int insertSysDataScopeConfig(SysDataScopeConfig config);

    /**
     * 修改数据权限配置
     */
    public int updateSysDataScopeConfig(SysDataScopeConfig config);

    /**
     * 删除数据权限配置
     */
    public int deleteSysDataScopeConfigById(Long id);
}
