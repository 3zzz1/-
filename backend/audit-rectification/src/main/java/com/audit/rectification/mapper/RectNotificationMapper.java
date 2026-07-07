package com.audit.rectification.mapper;

import java.util.List;
import com.audit.rectification.domain.RectNotification;

/**
 * 整改通知Mapper接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface RectNotificationMapper {

    /**
     * 查询通知列表
     *
     * @param notification 通知对象
     * @return 通知集合
     */
    public List<RectNotification> selectRectNotificationList(RectNotification notification);

    /**
     * 新增通知
     *
     * @param notification 通知对象
     * @return 结果
     */
    public int insertRectNotification(RectNotification notification);

    /**
     * 标记通知为已读
     *
     * @param notification 通知对象
     * @return 结果
     */
    public int updateRectNotification(RectNotification notification);

    /**
     * 查询未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public int selectUnreadCount(Long userId);
}
