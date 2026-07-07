-- ============================================
-- 智慧审计平台 - 模块八：分级细粒度权限管理体系 扩展表
-- ============================================

-- 1. 时效角色分配表（中介/临时授权）
DROP TABLE IF EXISTS sys_user_role_timed;
CREATE TABLE sys_user_role_timed (
    id              BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT(20)    NOT NULL COMMENT '用户ID',
    role_id         BIGINT(20)    NOT NULL COMMENT '角色ID',
    project_id      BIGINT(20)    DEFAULT NULL COMMENT '关联项目ID(中介授权场景)',
    effective_time  DATETIME      NOT NULL COMMENT '生效时间',
    expire_time     DATETIME      NOT NULL COMMENT '过期时间',
    grant_reason    VARCHAR(500)  DEFAULT '' COMMENT '授权原因',
    grant_user_id   BIGINT(20)    DEFAULT NULL COMMENT '授权人ID',
    status          CHAR(1)       DEFAULT '0' COMMENT '状态: 0=有效 1=已过期 2=已手动回收',
    revoke_time     DATETIME      DEFAULT NULL COMMENT '回收时间',
    create_by       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      COMMENT '更新时间',
    remark          VARCHAR(500)  DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    KEY idx_expire_time (expire_time),
    KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='时效角色分配表';

-- 2. 数据权限范围配置表（扩展Ruoyi内置data_scope）
DROP TABLE IF EXISTS sys_data_scope_config;
CREATE TABLE sys_data_scope_config (
    id              BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id         BIGINT(20)    NOT NULL COMMENT '角色ID',
    scope_field     VARCHAR(50)   NOT NULL COMMENT '过滤字段: dept_id / project_id / create_by',
    scope_value     VARCHAR(50)   NOT NULL COMMENT '取值来源: SELF_DEPT / ASSIGNED_PROJECTS / CURRENT_USER / ALL',
    scope_table     VARCHAR(100)  DEFAULT '' COMMENT '适用表名(多表逗号分隔)',
    create_time     DATETIME      COMMENT '创建时间',
    update_time     DATETIME      COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据权限范围配置表';

-- 3. 权限操作审计日志表
DROP TABLE IF EXISTS sys_permission_audit_log;
CREATE TABLE sys_permission_audit_log (
    log_id          BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT(20)    DEFAULT NULL COMMENT '操作用户ID',
    user_name       VARCHAR(100)  DEFAULT '' COMMENT '操作用户名',
    operation_type  VARCHAR(30)   DEFAULT '' COMMENT '操作类型: GRANT=授权 REVOKE=回收 EXPIRE=过期 ACCESS_DENIED=越权拒绝',
    target_type     VARCHAR(30)   DEFAULT '' COMMENT '目标类型: USER=用户 ROLE=角色 DATA=数据访问',
    target_id       BIGINT(20)    DEFAULT NULL COMMENT '目标ID',
    operation_detail TEXT         COMMENT '操作详情(JSON格式)',
    ip_address      VARCHAR(128)  DEFAULT '' COMMENT 'IP地址',
    operation_time  DATETIME      DEFAULT NULL COMMENT '操作时间',
    create_time     DATETIME      COMMENT '创建时间',
    PRIMARY KEY (log_id),
    KEY idx_user_id (user_id),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='权限操作审计日志表';

-- ============================================
-- 初始化权限配置数据
-- ============================================

-- 插入数据权限配置（示例：被审计单位联络员只能看本部门数据）
INSERT INTO sys_data_scope_config (role_id, scope_field, scope_value, scope_table) VALUES
((SELECT role_id FROM sys_role WHERE role_key = 'audited_unit_liaison' LIMIT 1), 'rect_dept_id', 'SELF_DEPT', 'rect_issue,rect_task'),
((SELECT role_id FROM sys_role WHERE role_key = 'audited_unit_leader' LIMIT 1), 'rect_dept_id', 'SELF_DEPT', 'rect_issue,rect_task'),
((SELECT role_id FROM sys_role WHERE role_key = 'audit_staff' LIMIT 1), 'create_by', 'CURRENT_USER', 'rect_issue'),
((SELECT role_id FROM sys_role WHERE role_key = 'external_auditor' LIMIT 1), 'project_id', 'ASSIGNED_PROJECTS', 'rect_issue,rect_task,rect_material');
