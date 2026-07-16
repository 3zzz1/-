package com.audit.rectification.service;

import java.util.List;
import com.audit.rectification.domain.RectExtension;

/**
 * 审计整改延期申请Service接口
 *
 * @author audit
 * @date 2026-07-06
 */
public interface IRectExtensionService {

    /**
     * 新增延期申请
     *
     * @param extension 延期申请对象
     * @return 结果
     */
    int insertRectExtension(RectExtension extension);

    /**
     * 审批延期申请
     *
     * @param extensionId 延期申请ID
     * @param status 审批状态
     * @param opinion 审批意见
     * @return 结果
     */
    int approveExtension(Long extensionId, String status, String opinion);

    RectExtension selectLatestByTaskId(Long taskId);

    List<RectExtension> selectPendingList();
}
