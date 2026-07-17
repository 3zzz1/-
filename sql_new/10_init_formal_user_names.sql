-- 正式演示人员姓名，可在已有数据库重复执行。
-- 用户名、角色、部门和密码均不改变，仅更新页面显示姓名。

UPDATE sys_user
SET nick_name = CASE user_name
    WHEN 'admin' THEN '系统管理员'
    WHEN 'school01' THEN '李建国'
    WHEN 'director01' THEN '张明远'
    WHEN 'lead01' THEN '陈志强'
    WHEN 'staff01' THEN '王海峰'
    WHEN 'unit01' THEN '李志华'
    WHEN 'link01' THEN '陈晓玲'
    WHEN 'rect01' THEN '王芳'
    WHEN 'rect06' THEN '李明'
    WHEN 'rect07' THEN '陈静'
    WHEN 'rect08' THEN '赵磊'
    WHEN 'unit02' THEN '王建军'
    WHEN 'link02' THEN '刘欣怡'
    WHEN 'rect02' THEN '周强'
    WHEN 'rect09' THEN '刘洋'
    WHEN 'rect10' THEN '孙悦'
    WHEN 'rect11' THEN '吴桐'
    WHEN 'unit03' THEN '赵国强'
    WHEN 'link03' THEN '周慧敏'
    WHEN 'rect03' THEN '何志鹏'
    WHEN 'unit04' THEN '钱志远'
    WHEN 'link04' THEN '孙宁'
    WHEN 'rect04' THEN '郑凯文'
    WHEN 'unit05' THEN '孙浩然'
    WHEN 'link05' THEN '吴倩'
    WHEN 'rect05' THEN '林峰'
    WHEN 'agent01' THEN '高俊杰'
    ELSE nick_name
END,
update_by = 'admin',
update_time = NOW()
WHERE user_name IN (
    'admin', 'school01', 'director01', 'lead01', 'staff01',
    'unit01', 'link01', 'rect01', 'rect06', 'rect07', 'rect08',
    'unit02', 'link02', 'rect02', 'rect09', 'rect10', 'rect11',
    'unit03', 'link03', 'rect03',
    'unit04', 'link04', 'rect04',
    'unit05', 'link05', 'rect05', 'agent01'
);
