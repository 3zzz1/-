-- ============================================
-- 审计整改管理模块 - 菜单和权限初始化数据
-- ============================================

-- 一级菜单：审计整改管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('审计整改管理', 0, 4, 'rectification', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'documentation', 'admin', sysdate(), '', NULL, '');

SET @rect_menu_id = LAST_INSERT_ID();

-- 子菜单：问题台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('问题台账', @rect_menu_id, 1, 'issue', 'rectification/issue/index', NULL, NULL, 1, 0, 'C', '0', '0', 'rectification:issue:list', 'list', 'admin', sysdate(), '', NULL, '');

SET @issue_menu_id = LAST_INSERT_ID();

-- 问题台账-按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('问题查询', @issue_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:query', '', 'admin', sysdate(), '', NULL, ''),
       ('问题新增', @issue_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:add', '', 'admin', sysdate(), '', NULL, ''),
       ('问题编辑', @issue_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:edit', '', 'admin', sysdate(), '', NULL, ''),
       ('问题删除', @issue_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:remove', '', 'admin', sysdate(), '', NULL, ''),
       ('问题同步', @issue_menu_id, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:sync', '', 'admin', sysdate(), '', NULL, ''),
       ('问题导出', @issue_menu_id, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:issue:export', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：整改任务
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('整改任务', @rect_menu_id, 2, 'task', 'rectification/task/index', NULL, NULL, 1, 0, 'C', '0', '0', 'rectification:task:list', 'task', 'admin', sysdate(), '', NULL, '');

SET @task_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('任务查询', @task_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:query', '', 'admin', sysdate(), '', NULL, ''),
       ('任务下发', @task_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:dispatch', '', 'admin', sysdate(), '', NULL, ''),
       ('批量下发', @task_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:batchDispatch', '', 'admin', sysdate(), '', NULL, ''),
       ('任务确认', @task_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:confirm', '', 'admin', sysdate(), '', NULL, ''),
       ('任务分办', @task_menu_id, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:assign', '', 'admin', sysdate(), '', NULL, ''),
       ('通知书生成', @task_menu_id, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:notice', '', 'admin', sysdate(), '', NULL, ''),
       ('逾期预警', @task_menu_id, 7, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:alert', '', 'admin', sysdate(), '', NULL, ''),
       ('我的任务', @task_menu_id, 8, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:task:myList', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：整改方案（隐藏在任务详情内操作）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('整改方案', @rect_menu_id, 3, 'plan', NULL, NULL, NULL, 1, 0, 'C', '1', '0', 'rectification:plan:query', 'edit', 'admin', sysdate(), '', NULL, '');

SET @plan_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('方案新增', @plan_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:plan:add', '', 'admin', sysdate(), '', NULL, ''),
       ('方案编辑', @plan_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:plan:edit', '', 'admin', sysdate(), '', NULL, ''),
       ('延期申请', @plan_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:plan:extension', '', 'admin', sysdate(), '', NULL, ''),
       ('长期整改', @plan_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:plan:longTerm', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：整改材料
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('整改材料', @rect_menu_id, 4, 'material', NULL, NULL, NULL, 1, 0, 'C', '1', '0', 'rectification:material:list', 'upload', 'admin', sysdate(), '', NULL, '');

SET @mat_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('材料上传', @mat_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:material:upload', '', 'admin', sysdate(), '', NULL, ''),
       ('材料删除', @mat_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:material:remove', '', 'admin', sysdate(), '', NULL, ''),
       ('材料下载', @mat_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:material:download', '', 'admin', sysdate(), '', NULL, ''),
       ('材料预览', @mat_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:material:preview', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：整改报告
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('整改报告', @rect_menu_id, 5, 'report', NULL, NULL, NULL, 1, 0, 'C', '1', '0', 'rectification:report:query', 'file', 'admin', sysdate(), '', NULL, '');

SET @rpt_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('报告新增', @rpt_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:report:add', '', 'admin', sysdate(), '', NULL, ''),
       ('报告生成', @rpt_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:report:generate', '', 'admin', sysdate(), '', NULL, ''),
       ('报告提交', @rpt_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:report:submit', '', 'admin', sysdate(), '', NULL, ''),
       ('领导审批', @rpt_menu_id, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:report:approve', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：销号管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销号管理', @rect_menu_id, 6, 'closure', 'rectification/closure/index', NULL, NULL, 1, 0, 'C', '0', '0', 'rectification:closure:list', 'checked', 'admin', sysdate(), '', NULL, '');

SET @cls_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销号查询', @cls_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:closure:query', '', 'admin', sysdate(), '', NULL, ''),
       ('销号申请', @cls_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:closure:apply', '', 'admin', sysdate(), '', NULL, ''),
       ('销号审核', @cls_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'rectification:closure:audit', '', 'admin', sysdate(), '', NULL, '');

-- 子菜单：整改分析
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('整改分析', @rect_menu_id, 7, 'statistics', 'rectification/statistics/index', NULL, NULL, 1, 0, 'C', '0', '0', 'rectification:statistics:view', 'chart', 'admin', sysdate(), '', NULL, '');
