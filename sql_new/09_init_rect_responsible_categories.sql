-- 学院具体整改责任人职责分类，可重复执行
SET @role_rect = (SELECT role_id FROM sys_role WHERE role_key='rect_responsible' LIMIT 1);

-- 经济管理学院：财务经费、资产采购、教学科研、人事管理
UPDATE sys_user SET nick_name='王芳', remark='财务经费类整改责任人'
WHERE user_name='rect01';
UPDATE sys_user SET nick_name='李明', remark='资产采购类整改责任人'
WHERE user_name='rect06';
UPDATE sys_user SET nick_name='陈静', remark='教学科研类整改责任人'
WHERE user_name='rect07';
UPDATE sys_user SET nick_name='赵磊', remark='人事管理类整改责任人'
WHERE user_name='rect08';

-- 信息工程学院：信息化建设、财务经费、资产采购、教学科研
UPDATE sys_user SET nick_name='周强', remark='信息化建设类整改责任人'
WHERE user_name='rect02';

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, del_flag, create_by, create_time, remark)
SELECT 'rect09', '刘洋', password, 202, 'rect09@university.edu.cn', '13900000019', '0', '0', '0', 'admin', sysdate(), '财务经费类整改责任人'
FROM sys_user WHERE user_name='rect02'
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='rect09')
LIMIT 1;
SET @rect09 = (SELECT user_id FROM sys_user WHERE user_name='rect09' LIMIT 1);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (@rect09, @role_rect);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, del_flag, create_by, create_time, remark)
SELECT 'rect10', '孙悦', password, 202, 'rect10@university.edu.cn', '13900000020', '1', '0', '0', 'admin', sysdate(), '资产采购类整改责任人'
FROM sys_user WHERE user_name='rect02'
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='rect10')
LIMIT 1;
SET @rect10 = (SELECT user_id FROM sys_user WHERE user_name='rect10' LIMIT 1);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (@rect10, @role_rect);

INSERT INTO sys_user (user_name, nick_name, password, dept_id, email, phonenumber, sex, status, del_flag, create_by, create_time, remark)
SELECT 'rect11', '吴桐', password, 202, 'rect11@university.edu.cn', '13900000021', '0', '0', '0', 'admin', sysdate(), '教学科研类整改责任人'
FROM sys_user WHERE user_name='rect02'
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='rect11')
LIMIT 1;
SET @rect11 = (SELECT user_id FROM sys_user WHERE user_name='rect11' LIMIT 1);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (@rect11, @role_rect);
