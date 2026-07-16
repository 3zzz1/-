-- 审计角色和权限修正，可在已有数据库重复执行
SET @role_admin = (SELECT role_id FROM sys_role WHERE role_key='admin' LIMIT 1);
SET @role_director = (SELECT role_id FROM sys_role WHERE role_key='audit_director' LIMIT 1);
SET @role_lead = (SELECT role_id FROM sys_role WHERE role_key='audit_lead' LIMIT 1);
SET @role_staff = (SELECT role_id FROM sys_role WHERE role_key='audit_staff' LIMIT 1);
SET @role_school = (SELECT role_id FROM sys_role WHERE role_key='school_leader' LIMIT 1);

UPDATE sys_role SET role_name='审计处长', data_scope='1' WHERE role_id=@role_director;
UPDATE sys_role SET role_name='项目组长/主审', data_scope='5' WHERE role_id=@role_lead;
UPDATE sys_role SET role_name='普通审计人员', data_scope='5' WHERE role_id=@role_staff;
UPDATE sys_user SET nick_name='系统管理员' WHERE user_name='admin';

-- 系统管理员只绑定系统管理员角色。
DELETE ur FROM sys_user_role ur JOIN sys_user u ON u.user_id=ur.user_id
WHERE u.user_name='admin' AND ur.role_id<>@role_admin;
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT user_id, @role_admin FROM sys_user WHERE user_name='admin';

-- 清理admin历史上被当作审计处长接收的整改业务通知，系统公告不受影响。
DELETE FROM rect_notification
WHERE recipient_user_id = (SELECT user_id FROM sys_user WHERE user_name='admin' LIMIT 1);

-- 审计处长为独立业务账号（密码：admin123）。
INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, create_by, create_time)
SELECT 'director01', '张处长', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 200,
       'director01@university.edu.cn', '13900000000', '0', '0', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='director01');
UPDATE sys_user
SET password='$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2'
WHERE user_name='director01' AND (password IS NULL OR LENGTH(password)<>60);
SET @director_user = (SELECT user_id FROM sys_user WHERE user_name='director01' LIMIT 1);
DELETE FROM sys_user_role WHERE user_id=@director_user;
INSERT IGNORE INTO sys_user_role VALUES (@director_user, @role_director);

DELETE FROM sys_role_menu WHERE role_id IN (@role_director, @role_lead, @role_staff, @role_school);

-- 审计处长：全部审计整改业务权限，不包含系统配置权限。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @role_director, menu_id FROM sys_menu
WHERE (perms LIKE 'rectification:%' AND perms NOT IN (
    'rectification:plan:query','rectification:plan:add','rectification:plan:edit',
    'rectification:plan:extension','rectification:plan:longTerm',
    'rectification:report:add','rectification:report:generate','rectification:report:submit','rectification:report:approve',
    'rectification:material:upload','rectification:material:remove'
)) OR (path='rectification' AND menu_type='M');

-- 项目组长/主审：问题维护、任务下发、方案变更审批、销号审核和整改分析。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @role_lead, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query','rectification:issue:add','rectification:issue:edit','rectification:issue:export',
    'rectification:task:list','rectification:task:query','rectification:task:dispatch','rectification:task:batchDispatch',
    'rectification:task:myList','rectification:task:notice','rectification:task:alert',
    'rectification:plan:change:approve',
    'rectification:material:list','rectification:material:download','rectification:material:preview',
    'rectification:report:query',
    'rectification:closure:list','rectification:closure:query','rectification:closure:audit',
    'rectification:statistics:view','rectification:progress:query','rectification:notification:query'
) OR (path='rectification' AND menu_type='M');

-- 普通审计人员：查看分配业务、材料和报告，查询进展并开展数据分析。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @role_staff, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query','rectification:task:myList',
    'rectification:material:list','rectification:material:download','rectification:material:preview',
    'rectification:report:query',
    'rectification:closure:list','rectification:closure:query',
    'rectification:statistics:view','rectification:progress:query','rectification:notification:query'
) OR (path='rectification' AND menu_type='M');

-- 校领导：全校整改汇总、任务进展、最终报告和销号结果只读。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @role_school, menu_id FROM sys_menu WHERE perms IN (
    'rectification:issue:list','rectification:issue:query',
    'rectification:task:list','rectification:task:query',
    'rectification:report:query','rectification:closure:list','rectification:closure:query',
    'rectification:statistics:view','rectification:progress:query','rectification:notification:query'
) OR (path='rectification' AND menu_type='M');
