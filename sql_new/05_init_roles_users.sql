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
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('项目组长', 'audit_lead', 2, '5', '0', 'admin', sysdate());
SET @role_lead = LAST_INSERT_ID();
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time) VALUES ('审计人员', 'audit_staff', 3, '5', '0', 'admin', sysdate());
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
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_director, menu_id FROM sys_menu WHERE perms LIKE 'rectification:%';
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_director, menu_id FROM sys_menu WHERE parent_id=1 OR menu_id=1;

-- 项目组长：操作+审核
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_lead, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query','rectification:issue:add',
    'rectification:task:list','rectification:task:query','rectification:task:dispatch',
    'rectification:task:batchDispatch','rectification:task:myList',
    'rectification:plan:query','rectification:plan:add','rectification:plan:edit','rectification:plan:change:approve',
    'rectification:material:list','rectification:material:upload','rectification:material:download',
    'rectification:report:query','rectification:report:add','rectification:report:submit',
    'rectification:closure:list','rectification:closure:query','rectification:closure:audit',
    'rectification:statistics:view','rectification:progress:query',
    'rectification:notification:list','rectification:notification:count');

-- 审计人员：录入+查看
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_staff, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query','rectification:issue:add',
    'rectification:task:list','rectification:task:query',
    'rectification:plan:query','rectification:plan:add',
    'rectification:material:list','rectification:material:upload',
    'rectification:report:query','rectification:report:add',
    'rectification:progress:query');

-- 校领导：只读
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_school, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query',
    'rectification:statistics:view');

-- 被审单位负责人：查看本单位
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_leader, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:query','rectification:task:myList','rectification:task:query',
    'rectification:plan:change:approve',
    'rectification:report:query','rectification:closure:query');

-- 被审单位联络员：操作本单位
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_liaison, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:query','rectification:task:myList','rectification:task:query',
    'rectification:task:confirm','rectification:task:assign',
    'rectification:plan:query','rectification:plan:add','rectification:plan:edit','rectification:plan:extension',
    'rectification:material:list','rectification:material:upload',
    'rectification:report:query','rectification:report:add','rectification:report:submit',
    'rectification:closure:query','rectification:closure:apply',
    'rectification:progress:query','rectification:notification:list','rectification:notification:count');

-- 中介：仅材料上传
INSERT INTO sys_role_menu (role_id, menu_id) SELECT @role_agent, menu_id FROM sys_menu WHERE perms IN (
    'rectification:material:list','rectification:material:upload',
    'rectification:task:myList','rectification:task:query');

-- 5. 更新admin为审计处长
UPDATE sys_user SET dept_id=200 WHERE user_name='admin';
DELETE FROM sys_user_role WHERE user_id=1;
INSERT INTO sys_user_role VALUES (1, @role_director);

-- 6. 创建示例用户 (密码: admin123)
INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('school01', '校领导A', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'school01@university.edu.cn', '13900000001', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_school);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('lead01', '张组长', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'lead01@university.edu.cn', '13900000002', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_lead);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('staff01', '王审计', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200, 'staff01@university.edu.cn', '13900000003', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_staff);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('unit01', '经管学院负责人', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 201, 'unit01@university.edu.cn', '13900000004', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_leader);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('link01', '经管学院联络员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 201, 'link01@university.edu.cn', '13900000005', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_liaison);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time) VALUES
('agent01', '中介审计员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 205, 'agent01@university.edu.cn', '13900000006', '0', '0', 'admin', sysdate());
SET @uid = LAST_INSERT_ID();
INSERT INTO sys_user_role VALUES (@uid, @role_agent);
