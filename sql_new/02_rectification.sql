-- ============================================
-- 智慧审计平台 - 模块三：审计整改管理 业务表
-- ============================================

-- 1. 整改问题台账（核心表）
DROP TABLE IF EXISTS rect_issue;
CREATE TABLE rect_issue (
    issue_id          BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    issue_no          VARCHAR(64)   NOT NULL COMMENT '问题编号(系统自动生成)',
    source_type       CHAR(1)       NOT NULL COMMENT '来源类型: 1=内源审计项目 2=外源巡视巡察 3=外部督查',
    source_project_id BIGINT(20)    DEFAULT NULL COMMENT '来源审计项目ID(内源)',
    source_desc       VARCHAR(500)  DEFAULT '' COMMENT '外源来源描述',
    issue_title       VARCHAR(500)  NOT NULL COMMENT '问题标题',
    issue_desc        TEXT          COMMENT '问题描述',
    issue_amount      DECIMAL(18,2) DEFAULT 0.00 COMMENT '涉及金额',
    legal_basis       TEXT          COMMENT '定性法规依据',
    responsible_dept_id  BIGINT(20) DEFAULT NULL COMMENT '责任单位ID(关联sys_dept)',
    responsible_person   VARCHAR(100) DEFAULT '' COMMENT '责任干部/责任人',
    issue_category    VARCHAR(50)   DEFAULT '' COMMENT '问题分类: FUND=资金类 ASSET=资产类 PURCHASE=采购类 HR=人事类 CONSTRUCTION=基建类 OTHER=其他',
    risk_level        CHAR(1)       DEFAULT '1' COMMENT '风险等级: 1=低 2=中 3=高',
    deadline          DATE          DEFAULT NULL COMMENT '整改截止日期',
    status            CHAR(1)       DEFAULT '0' COMMENT '状态: 0=待下发 1=整改中 2=待审核 3=已销号 4=持续整改',
    closure_date      DATETIME      DEFAULT NULL COMMENT '销号日期',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_issue_no (issue_no),
    KEY idx_responsible_dept (responsible_dept_id),
    KEY idx_status (status),
    KEY idx_source_type (source_type),
    KEY idx_issue_category (issue_category)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改问题台账';

-- 2. 整改任务
DROP TABLE IF EXISTS rect_task;
CREATE TABLE rect_task (
    task_id           BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    task_no           VARCHAR(64)   NOT NULL COMMENT '任务编号(系统自动生成)',
    issue_ids         TEXT          NOT NULL COMMENT '关联问题ID列表(JSON数组)',
    rect_dept_id      BIGINT(20)    NOT NULL COMMENT '整改单位ID(关联sys_dept)',
    contact_person    VARCHAR(100)  DEFAULT '' COMMENT '联络人',
    contact_phone     VARCHAR(20)   DEFAULT '' COMMENT '联络电话',
    task_requirement  TEXT          COMMENT '整改要求',
    deadline          DATE          NOT NULL COMMENT '整改截止日期',
    notice_file_path  VARCHAR(500)  DEFAULT '' COMMENT '整改通知书Word文件路径',
    status            CHAR(1)       DEFAULT '0' COMMENT '状态: 0=待确认 1=整改中 2=已提交报告 3=待审核 4=已完成 5=已驳回',
    dispatch_user_id  BIGINT(20)    DEFAULT NULL COMMENT '下发人ID',
    dispatch_time     DATETIME      DEFAULT NULL COMMENT '下发时间',
    confirm_time      DATETIME      DEFAULT NULL COMMENT '确认接收时间',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_rect_dept (rect_dept_id),
    KEY idx_status (status),
    KEY idx_deadline (deadline)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改任务';

-- 3. 整改方案
DROP TABLE IF EXISTS rect_plan;
CREATE TABLE rect_plan (
    plan_id           BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '方案ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    issue_id          BIGINT(20)    NOT NULL COMMENT '关联问题ID',
    plan_content      TEXT          COMMENT '整改方案内容(富文本)',
    responsible_user_id BIGINT(20)  DEFAULT NULL COMMENT '整改责任人ID',
    plan_deadline     DATE          DEFAULT NULL COMMENT '计划完成日期',
    plan_type         CHAR(1)       DEFAULT '1' COMMENT '整改类型: 1=正常整改 2=长期持续整改',
    long_term_reason  TEXT          COMMENT '长期整改原因说明',
    status            CHAR(1)       DEFAULT '0' COMMENT '状态: 0=草稿 1=已提交',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (plan_id),
    KEY idx_task_id (task_id),
    KEY idx_issue_id (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改方案';

-- 4. 整改佐证材料
DROP TABLE IF EXISTS rect_material;
CREATE TABLE rect_material (
    material_id       BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '材料ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    issue_id          BIGINT(20)    NOT NULL COMMENT '关联问题ID',
    material_type     VARCHAR(50)   DEFAULT 'OTHER' COMMENT '材料类型: CONTRACT=合同 VOUCHER=凭证 POLICY=制度文件 PHOTO=照片 OTHER=其他',
    file_name         VARCHAR(255)  NOT NULL COMMENT '文件名',
    file_path         VARCHAR(500)  NOT NULL COMMENT '文件存储路径',
    file_size         BIGINT(20)    DEFAULT 0 COMMENT '文件大小(字节)',
    file_ext          VARCHAR(20)   DEFAULT '' COMMENT '文件扩展名',
    upload_user_id    BIGINT(20)    DEFAULT NULL COMMENT '上传人ID',
    upload_time       DATETIME      DEFAULT NULL COMMENT '上传时间',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (material_id),
    KEY idx_issue_id (issue_id),
    KEY idx_task_id (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改佐证材料';

-- 5. 整改进展记录（时间轴）
DROP TABLE IF EXISTS rect_progress;
CREATE TABLE rect_progress (
    progress_id       BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '进展ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    issue_id          BIGINT(20)    DEFAULT NULL COMMENT '关联问题ID',
    progress_type     VARCHAR(30)   NOT NULL COMMENT '进展类型: DISPATCH/CONFIRM/ASSIGN/PLAN_SUBMIT/MATERIAL_UPLOAD/REPORT_SUBMIT/LEADER_APPROVE/CLOSURE_APPLY/CLOSURE_AUDIT/EXTENSION_APPLY/EXTENSION_APPROVE/REJECT',
    content           TEXT          NOT NULL COMMENT '进展描述',
    operator_id       BIGINT(20)    DEFAULT NULL COMMENT '操作人ID',
    operator_name     VARCHAR(100)  DEFAULT '' COMMENT '操作人姓名',
    operate_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time       DATETIME      COMMENT '创建时间',
    PRIMARY KEY (progress_id),
    KEY idx_task_id (task_id),
    KEY idx_issue_id (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改进展记录(时间轴)';

-- 6. 整改报告
DROP TABLE IF EXISTS rect_report;
CREATE TABLE rect_report (
    report_id         BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    report_content    TEXT          COMMENT '报告内容(富文本)',
    generated_path    VARCHAR(500)  DEFAULT '' COMMENT '自动生成的Word文件路径',
    status            CHAR(1)       DEFAULT '0' COMMENT '报告状态: 0=草稿 1=已提交 2=单位已审批 3=审计处已接收',
    submit_user_id    BIGINT(20)    DEFAULT NULL COMMENT '提交人ID',
    submit_time       DATETIME      DEFAULT NULL COMMENT '提交时间',
    unit_approve_status CHAR(1)     DEFAULT '0' COMMENT '单位领导审批: 0=待审批 1=已通过 2=已驳回',
    unit_approve_user_id BIGINT(20) DEFAULT NULL COMMENT '单位领导审批人ID',
    unit_approve_time DATETIME      DEFAULT NULL COMMENT '单位领导审批时间',
    unit_approve_opinion VARCHAR(500) DEFAULT '' COMMENT '单位领导审批意见',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (report_id),
    KEY idx_task_id (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改报告';

-- 7. 整改方案变更申请（延期/长期持续整改）
DROP TABLE IF EXISTS rect_extension;
CREATE TABLE rect_extension (
    extension_id      BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '方案变更申请ID',
    issue_id          BIGINT(20)    NOT NULL COMMENT '关联问题ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    apply_type        CHAR(1)       NOT NULL DEFAULT '1' COMMENT '申请类型: 1=延期 2=长期持续整改',
    original_deadline DATE          DEFAULT NULL COMMENT '原截止日期',
    extension_days    INT(11)       DEFAULT NULL COMMENT '申请延期天数',
    new_deadline      DATE          DEFAULT NULL COMMENT '新截止日期',
    reason            TEXT          NOT NULL COMMENT '申请原因',
    stage_goal        TEXT          COMMENT '阶段整改目标',
    review_date       DATE          DEFAULT NULL COMMENT '下一次复核日期',
    expected_finish_date DATE       DEFAULT NULL COMMENT '预计最终完成日期',
    status            CHAR(1)       DEFAULT '0' COMMENT '状态: 0=单位审批中 1=审计处审批中 2=已通过 3=单位驳回 4=审计处驳回',
    apply_user_id     BIGINT(20)    DEFAULT NULL COMMENT '申请人ID',
    apply_time        DATETIME      DEFAULT NULL COMMENT '申请时间',
    approve_user_id   BIGINT(20)    DEFAULT NULL COMMENT '单位审批人ID',
    approve_time      DATETIME      DEFAULT NULL COMMENT '单位审批时间',
    approve_opinion   VARCHAR(500)  DEFAULT '' COMMENT '单位审批意见',
    audit_approve_user_id BIGINT(20) DEFAULT NULL COMMENT '审计处审批人ID',
    audit_approve_time DATETIME     DEFAULT NULL COMMENT '审计处审批时间',
    audit_approve_opinion VARCHAR(500) DEFAULT '' COMMENT '审计处审批意见',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (extension_id),
    KEY idx_issue_id (issue_id),
    KEY idx_task_id (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='整改方案变更申请';

-- 8. 销号申请
DROP TABLE IF EXISTS rect_closure;
CREATE TABLE rect_closure (
    closure_id        BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '销号申请ID',
    issue_id          BIGINT(20)    NOT NULL COMMENT '关联问题ID',
    task_id           BIGINT(20)    NOT NULL COMMENT '关联任务ID',
    apply_content     TEXT          COMMENT '整改完成情况说明',
    status            CHAR(1)       DEFAULT '0' COMMENT '状态: 0=待审核 1=已销号 2=已驳回',
    apply_user_id     BIGINT(20)    DEFAULT NULL COMMENT '申请人ID',
    apply_time        DATETIME      DEFAULT NULL COMMENT '申请时间',
    audit_user_id     BIGINT(20)    DEFAULT NULL COMMENT '审核人ID(审计处)',
    audit_time        DATETIME      DEFAULT NULL COMMENT '审核时间',
    audit_result      CHAR(1)       DEFAULT NULL COMMENT '审核结果: 1=整改完成销号 2=整改不到位驳回',
    audit_opinion     TEXT          COMMENT '审核意见',
    re_rect_required  VARCHAR(500)  DEFAULT '' COMMENT '补充整改要求(驳回时填写)',
    del_flag          CHAR(1)       DEFAULT '0' COMMENT '删除标志(0=存在 2=删除)',
    create_by         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time       DATETIME      COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time       DATETIME      COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (closure_id),
    KEY idx_issue_id (issue_id),
    KEY idx_task_id (task_id),
    KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='销号申请';

-- 9. 消息通知记录
DROP TABLE IF EXISTS rect_notification;
CREATE TABLE rect_notification (
    notification_id   BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    task_id           BIGINT(20)    DEFAULT NULL COMMENT '关联任务ID',
    issue_id          BIGINT(20)    DEFAULT NULL COMMENT '关联问题ID',
    notify_type       VARCHAR(20)   DEFAULT 'SYSTEM_MSG' COMMENT '通知类型: SYSTEM_MSG=站内消息 SMS=短信 WECHAT=企业微信',
    title             VARCHAR(255)  NOT NULL COMMENT '通知标题',
    content           TEXT          NOT NULL COMMENT '通知内容',
    recipient_user_id BIGINT(20)    DEFAULT NULL COMMENT '接收人ID',
    recipient_phone   VARCHAR(20)   DEFAULT '' COMMENT '接收手机号(SMS场景)',
    send_status       CHAR(1)       DEFAULT '0' COMMENT '发送状态: 0=待发送 1=已发送 2=发送失败',
    send_time         DATETIME      DEFAULT NULL COMMENT '发送时间',
    read_status       CHAR(1)       DEFAULT '0' COMMENT '阅读状态: 0=未读 1=已读',
    read_time         DATETIME      DEFAULT NULL COMMENT '阅读时间',
    create_time       DATETIME      COMMENT '创建时间',
    PRIMARY KEY (notification_id),
    KEY idx_recipient (recipient_user_id),
    KEY idx_read_status (read_status),
    KEY idx_task_id (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='消息通知记录';
