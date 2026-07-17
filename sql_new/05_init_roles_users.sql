-- ============================================
-- 智慧审计平台 - 清理假数据 + 创建角色 + 示例用户
-- ============================================

-- 1. 清理
DELETE FROM sys_dept WHERE dept_id >= 100;
DELETE FROM sys_notice;
DELETE FROM sys_menu WHERE parent_id IN (2,3,4);
DELETE FROM sys_user WHERE user_name='ry';
UPDATE sys_user SET nick_name='系统管理员' WHERE user_name='admin';

-- 2. 高校部门
INSERT INTO sys_dept VALUES (200, 0, '0', '审计处', 0, '张处长', '13800000001', 'sjc@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);
INSERT INTO sys_dept VALUES (201, 0, '0', '经济管理学院', 0, '李院长', '13800000002', 'jg@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);
INSERT INTO sys_dept VALUES (202, 0, '0', '信息工程学院', 0, '王院长', '13800000003', 'xx@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);
INSERT INTO sys_dept VALUES (203, 0, '0', '后勤保障处', 0, '赵处长', '13800000004', 'hq@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);
INSERT INTO sys_dept VALUES (204, 0, '0', '基建处', 0, '钱处长', '13800000005', 'jj@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);
INSERT INTO sys_dept VALUES (205, 0, '0', '校办企业集团', 0, '孙总', '13800000006', 'xb@university.edu.cn', '0', '0', 'admin', sysdate(), '', NULL);

-- 3. 7类角色
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('审计处长', 'audit_director', 1, '1', '0', 'admin', sysdate());
SET @role_director = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('项目组长/主审', 'audit_lead', 2, '5', '0', 'admin', sysdate());
SET @role_lead = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('普通审计人员', 'audit_staff', 3, '5', '0', 'admin', sysdate());
SET @role_staff = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('校领导', 'school_leader', 4, '1', '0', 'admin', sysdate());
SET @role_school = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('被审单位负责人', 'audited_unit_leader', 5, '3', '0', 'admin', sysdate());
SET @role_leader = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('被审单位联络员', 'audited_unit_liaison', 6, '3', '0', 'admin', sysdate());
SET @role_liaison = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('中介机构人员', 'external_auditor', 7, '2', '0', 'admin', sysdate());
SET @role_agent = LAST_INSERT_ID();

-- 4. 菜单权限分配

-- 审计处长：全部
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_director, menu_id FROM sys_menu
WHERE (perms LIKE 'rectification:%' AND perms NOT IN (
    'rectification:plan:query','rectification:plan:add','rectification:plan:edit',
    'rectification:plan:extension','rectification:plan:longTerm',
    'rectification:report:add','rectification:report:generate','rectification:report:submit','rectification:report:approve',
    'rectification:material:upload','rectification:material:remove'
));
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_director, menu_id FROM sys_menu WHERE path='rectification' AND menu_type='M';

-- 项目组长/主审：问题维护、任务下发和整改审核
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_lead, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query','rectification:issue:add','rectification:issue:edit','rectification:issue:export',
    'rectification:task:list','rectification:task:query','rectification:task:dispatch',
    'rectification:task:batchDispatch','rectification:task:myList','rectification:task:notice','rectification:task:alert',
    'rectification:plan:change:approve',
    'rectification:material:list','rectification:material:download','rectification:material:preview',
    'rectification:report:query',
    'rectification:closure:list','rectification:closure:query','rectification:closure:audit',
    'rectification:statistics:view','rectification:progress:query',
    'rectification:notification:query');
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_lead, menu_id FROM sys_menu WHERE path='rectification' AND menu_type='M';

-- 普通审计人员：查看分配业务和整改分析
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_staff, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query','rectification:task:myList',
    'rectification:material:list','rectification:material:download','rectification:material:preview',
    'rectification:report:query',
    'rectification:closure:list','rectification:closure:query',
    'rectification:statistics:view','rectification:progress:query','rectification:notification:query');
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_staff, menu_id FROM sys_menu WHERE path='rectification' AND menu_type='M';

-- 校领导：只读
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_school, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query',
    'rectification:report:query','rectification:closure:list','rectification:closure:query',
    'rectification:statistics:view','rectification:progress:query','rectification:notification:query');
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_school, menu_id FROM sys_menu WHERE path='rectification' AND menu_type='M';

-- 被审单位负责人：查看本单位
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_leader, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:query','rectification:task:myList','rectification:task:query',
    'rectification:plan:change:approve',
    'rectification:report:query','rectification:closure:query');

-- 被审单位联络员：操作本单位
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_liaison, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:query','rectification:task:myList','rectification:task:query',
    'rectification:task:confirm','rectification:task:assign',
    'rectification:plan:add',
    'rectification:material:list','rectification:material:upload',
    'rectification:closure:query',
    'rectification:progress:query','rectification:notification:query');

-- 中介：仅查看有效授权项目的问题和整改进展
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_agent, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query',
    'rectification:progress:query');
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_agent, menu_id FROM sys_menu WHERE path='rectification' AND menu_type='M';

-- 5. admin保留系统管理员角色，审计处长使用独立业务账号
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id FROM sys_user u JOIN sys_role r ON r.role_key='admin'
WHERE u.user_name='admin';

-- 6. 创建示例用户 (密码: admin123)
INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time)
SELECT 'director01', '张明远', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200,
       'director01@university.edu.cn', '13900000000', '0', '0', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='director01');
SET @uid = (SELECT user_id FROM sys_user WHERE user_name='director01' LIMIT 1);
INSERT IGNORE INTO sys_user_role VALUES (@uid, @role_director);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('school01', '李建国', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'school01@university.edu.cn', '13900000001', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_school);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('lead01', '陈志强', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'lead01@university.edu.cn', '13900000002', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_lead);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('staff01', '王海峰', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'staff01@university.edu.cn', '13900000003', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_staff);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('unit01', '李志华', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 201, 'unit01@university.edu.cn', '13900000004', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_leader);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('link01', '陈晓玲', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 201, 'link01@university.edu.cn', '13900000005', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_liaison);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('agent01', '高俊杰', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 205, 'agent01@university.edu.cn', '13900000006', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_agent);
