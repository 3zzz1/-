package com.ruoyi.framework.interceptor;

import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRoleTimed;
import com.ruoyi.system.mapper.SysUserRoleTimedMapper;

/**
 * 时效权限校验拦截器
 * 拦截所有请求，检查当前用户的时效角色是否仍在有效期内
 * 适用于中介机构人员（contractor）等临时授权场景
 *
 * @author audit
 */
@Component
public class TimedPermissionInterceptor implements HandlerInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(TimedPermissionInterceptor.class);

    /**
     * 跳过权限校验的请求路径前缀（登录、登出、验证码等）
     */
    private static final String[] EXCLUDE_PATH_PREFIXES = {
        "/login", "/logout", "/captcha", "/register",
        "/common/download", "/common/download/resource"
    };

    @Autowired(required = false)
    private SysUserRoleTimedMapper sysUserRoleTimedMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        // 检查是否为需要跳过鉴权的请求路径
        String requestUri = request.getRequestURI();
        if (isExcludedPath(requestUri))
        {
            return true;
        }

        // 获取当前登录用户
        LoginUser loginUser;
        try
        {
            loginUser = SecurityUtils.getLoginUser();
        }
        catch (Exception e)
        {
            // 未登录用户由Spring Security处理，此处放行
            return true;
        }

        if (loginUser == null || loginUser.getUser() == null)
        {
            return true;
        }

        // 管理员不受时效权限限制
        if (loginUser.getUser().isAdmin())
        {
            return true;
        }

        Long userId = loginUser.getUserId();
        if (userId == null)
        {
            return true;
        }

        // 如果mapper未注入（sys_user_role_timed表可能不存在），放行所有请求
        if (sysUserRoleTimedMapper == null)
        {
            return true;
        }

        // 查询当前用户是否有时效角色分配
        List<SysUserRoleTimed> timedRoles;
        try
        {
            timedRoles = sysUserRoleTimedMapper.selectByUserId(userId);
        }
        catch (Exception e)
        {
            log.warn("查询用户[{}]时效角色异常: {}", userId, e.getMessage());
            // 查询异常时放行，避免误拦截
            return true;
        }

        // 无时效角色分配，按常规权限处理，放行
        if (StringUtils.isEmpty(timedRoles))
        {
            return true;
        }

        Date now = new Date();
        boolean hasExpiredRole = false;
        StringBuilder expiredInfo = new StringBuilder();

        for (SysUserRoleTimed timed : timedRoles)
        {
            // 状态为'0'=有效的才需要检查过期
            if ("0".equals(timed.getStatus()))
            {
                if (timed.getExpireTime() != null && timed.getExpireTime().before(now))
                {
                    hasExpiredRole = true;
                    expiredInfo.append(StringUtils.format(
                            "[角色ID:{}, 过期时间:{}] ",
                            timed.getRoleId(),
                            timed.getExpireTime()));
                }
            }
            else if ("1".equals(timed.getStatus()) || "2".equals(timed.getStatus()))
            {
                // 已过期或已回收的角色
                hasExpiredRole = true;
                expiredInfo.append(StringUtils.format(
                        "[角色ID:{}, 状态:{}, 回收时间:{}] ",
                        timed.getRoleId(),
                        "1".equals(timed.getStatus()) ? "已过期" : "已手动回收",
                        timed.getRevokeTime()));
            }
        }

        // 如果存在过期或已失效的时效角色，并且用户没有其他有效角色
        if (hasExpiredRole)
        {
            // 检查用户是否还有其他有效角色（check if user has any non-expired, active roles）
            boolean hasAnyActive = timedRoles.stream()
                    .anyMatch(t -> "0".equals(t.getStatus())
                            && (t.getExpireTime() == null || t.getExpireTime().after(now)));

            if (!hasAnyActive)
            {
                log.warn("用户[{}]的时效角色已全部过期或失效，拒绝访问。过期信息: {}",
                        loginUser.getUsername(), expiredInfo.toString());

                AjaxResult ajaxResult = AjaxResult.error(
                        "您的临时权限已过期，请联系管理员重新授权或申请延期。");
                ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
                return false;
            }
            else
            {
                log.info("用户[{}]部分时效角色已过期，但仍存在有效角色，允许访问。过期角色: {}",
                        loginUser.getUsername(), expiredInfo.toString());
            }
        }

        return true;
    }

    /**
     * 判断请求路径是否在排除列表中
     */
    private boolean isExcludedPath(String requestUri)
    {
        if (StringUtils.isEmpty(requestUri))
        {
            return false;
        }

        for (String prefix : EXCLUDE_PATH_PREFIXES)
        {
            if (requestUri.startsWith(prefix))
            {
                return true;
            }
        }
        return false;
    }
}
