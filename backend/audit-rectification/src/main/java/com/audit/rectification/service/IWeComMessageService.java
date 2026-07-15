package com.audit.rectification.service;

import com.ruoyi.common.core.domain.entity.SysUser;

public interface IWeComMessageService {

    void sendTaskNotice(SysUser user, Long taskId, String title, String content);
}
