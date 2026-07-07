package com.ruoyi.framework.aspectj;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDataScopeConfig;
import com.ruoyi.system.mapper.SysDataScopeConfigMapper;

/**
 * 整改模块数据权限AOP切面
 * 拦截MyBatis Mapper方法，注入数据范围过滤条件
 * 适配整改相关表（rect_*）的数据权限过滤逻辑
 *
 * @author audit
 */
@Aspect
@Component
public class RectDataScopeAspect
{
    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    /**
     * 审计处长角色标识
     */
    private static final String ROLE_AUDIT_DIRECTOR = "audit_director";

    @Autowired(required = false)
    private SysDataScopeConfigMapper dataScopeConfigMapper;

    @Pointcut("@annotation(com.ruoyi.framework.aspectj.RectDataScopeAspect.RectDataScope)")
    public void dataScopePointCut()
    {
    }

    /**
     * 在Mapper方法执行前注入数据权限过滤条件
     */
    @Before("dataScopePointCut() && @annotation(rectDataScope)")
    public void doBefore(JoinPoint point, RectDataScope rectDataScope) throws Throwable
    {
        // 清除之前的数据权限参数，防止注入
        clearDataScope(point);
        // 处理数据权限过滤
        handleDataScope(point, rectDataScope);
    }

    /**
     * 清除params中的dataScope参数
     */
    private void clearDataScope(final JoinPoint joinPoint)
    {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }

    /**
     * 处理数据权限过滤逻辑
     *
     * @param joinPoint 切点
     * @param rectDataScope 整改数据权限注解
     */
    protected void handleDataScope(final JoinPoint joinPoint, RectDataScope rectDataScope)
    {
        // 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser))
        {
            return;
        }

        SysUser currentUser = loginUser.getUser();
        if (StringUtils.isNull(currentUser))
        {
            return;
        }

        // 管理员拥有所有数据权限，不需要过滤
        if (currentUser.isAdmin())
        {
            return;
        }

        String deptAlias = StringUtils.defaultString(rectDataScope.deptAlias(), "");
        String userAlias = StringUtils.defaultString(rectDataScope.userAlias(), "");

        // 构建数据权限过滤SQL
        StringBuilder sqlString = new StringBuilder();
        List<SysRole> roles = currentUser.getRoles();

        if (StringUtils.isNull(roles) || roles.isEmpty())
        {
            // 无角色用户：不授予任何数据权限
            sqlString.append(StringUtils.format(" OR 1 = 0 "));
        }
        else
        {
            boolean hasAuditDirector = isAuditDirector(roles);

            // 如果用户拥有审计处长角色，则拥有全部数据权限
            if (hasAuditDirector)
            {
                return;
            }

            // 遍历角色，生成数据权限过滤SQL
            for (SysRole role : roles)
            {
                // 停用的角色跳过
                if (!"0".equals(role.getStatus()))
                {
                    continue;
                }

                // 从sys_data_scope_config表获取数据权限配置
                if (dataScopeConfigMapper != null)
                {
                    List<SysDataScopeConfig> scopeConfigs = dataScopeConfigMapper.selectByRoleId(role.getRoleId());
                    if (StringUtils.isNotEmpty(scopeConfigs))
                    {
                        for (SysDataScopeConfig config : scopeConfigs)
                        {
                            appendScopeCondition(sqlString, config, deptAlias, userAlias, currentUser);
                        }
                        continue;
                    }
                }

                // 无配置时，使用默认的基于部门的数据过滤
                if (currentUser.getDeptId() != null && StringUtils.isNotEmpty(deptAlias))
                {
                    sqlString.append(StringUtils.format(
                            " OR {}.rect_dept_id = {} ", deptAlias, currentUser.getDeptId()));
                }
            }

            // 如果所有角色都没有生成过滤条件，则限制为不查询任何数据
            if (StringUtils.isBlank(sqlString.toString()))
            {
                if (StringUtils.isNotEmpty(userAlias))
                {
                    // 仅本人数据
                    sqlString.append(StringUtils.format(
                            " OR {}.create_by = '{}' ", userAlias, currentUser.getUserName()));
                }
                else if (StringUtils.isNotEmpty(deptAlias))
                {
                    sqlString.append(StringUtils.format(
                            " OR {}.rect_dept_id = {} ", deptAlias, currentUser.getDeptId() != null ? currentUser.getDeptId() : 0));
                }
                else
                {
                    sqlString.append(" OR 1 = 0 ");
                }
            }
        }

        // 将数据权限过滤SQL注入到参数对象中
        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                // 去掉最前面的 " OR "，并用 AND 包裹
                String scopeSql = sqlString.toString().trim();
                if (scopeSql.startsWith("OR "))
                {
                    scopeSql = scopeSql.substring(3).trim();
                }
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + scopeSql + ")");
            }
        }
    }

    /**
     * 根据SysDataScopeConfig配置追加过滤条件
     */
    private void appendScopeCondition(StringBuilder sqlString, SysDataScopeConfig config,
            String deptAlias, String userAlias, SysUser currentUser)
    {
        String scopeField = config.getScopeField();
        String scopeValue = config.getScopeValue();
        String scopeTable = config.getScopeTable();

        if (StringUtils.isEmpty(scopeField))
        {
            return;
        }

        // scopeValue支持变量替换: ${userId}, ${deptId}, ${userName}
        String resolvedValue = resolveScopeValue(scopeValue, currentUser);

        // 根据scopeField生成不同的过滤条件
        switch (scopeField)
        {
            case "rect_dept_id":
                if (StringUtils.isNotEmpty(deptAlias))
                {
                    sqlString.append(StringUtils.format(
                            " OR {}.rect_dept_id = {} ", deptAlias, resolvedValue));
                }
                break;
            case "create_by":
                if (StringUtils.isNotEmpty(userAlias))
                {
                    sqlString.append(StringUtils.format(
                            " OR {}.create_by = '{}' ", userAlias, resolvedValue));
                }
                break;
            case "custom_sql":
                // 直接使用scopeValue作为定制SQL片段
                if (StringUtils.isNotEmpty(scopeValue))
                {
                    sqlString.append(" OR ").append(resolvedValue).append(" ");
                }
                break;
            default:
                // 通用字段过滤：使用scopeField和scopeValue
                if (scopeField.contains("."))
                {
                    sqlString.append(StringUtils.format(
                            " OR {} = '{}' ", scopeField, resolvedValue));
                }
                else if (StringUtils.isNotEmpty(deptAlias))
                {
                    sqlString.append(StringUtils.format(
                            " OR {}.{} = '{}' ", deptAlias, scopeField, resolvedValue));
                }
                break;
        }
    }

    /**
     * 解析scopeValue中的变量占位符
     * 支持: ${userId} -> 当前用户ID
     *       ${deptId} -> 当前部门ID
     *       ${userName} -> 当前用户名
     */
    private String resolveScopeValue(String scopeValue, SysUser currentUser)
    {
        if (StringUtils.isEmpty(scopeValue))
        {
            return scopeValue;
        }

        String result = scopeValue;
        result = result.replace("${userId}", String.valueOf(currentUser.getUserId()));
        result = result.replace("${deptId}",
                currentUser.getDeptId() != null ? String.valueOf(currentUser.getDeptId()) : "0");
        result = result.replace("${userName}",
                currentUser.getUserName() != null ? currentUser.getUserName() : "");

        return result;
    }

    /**
     * 判断用户是否拥有审计处长角色
     */
    private boolean isAuditDirector(List<SysRole> roles)
    {
        if (StringUtils.isNull(roles))
        {
            return false;
        }
        return roles.stream().anyMatch(r -> ROLE_AUDIT_DIRECTOR.equals(r.getRoleKey())
                && "0".equals(r.getStatus()));
    }

    /**
     * 整改模块数据权限注解
     * 用于标注Mapper方法，声明该查询需要进行数据权限过滤
     *
     * @author audit
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RectDataScope
    {
        /**
         * 部门字段所在表的别名（例: "d" 代表 FROM rect_problem d）
         */
        String deptAlias() default "";

        /**
         * 用户字段所在表的别名（例: "u" 代表 LEFT JOIN sys_user u）
         */
        String userAlias() default "";
    }
}
