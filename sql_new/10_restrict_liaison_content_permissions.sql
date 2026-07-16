-- 联络员仅负责接收、分办和协调，不查看整改方案、整改报告正文
SET @role_liaison = (SELECT role_id FROM sys_role WHERE role_key='audited_unit_liaison' LIMIT 1);

DELETE rm
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id=rm.menu_id
WHERE rm.role_id=@role_liaison
  AND m.perms IN (
    'rectification:plan:query',
    'rectification:plan:edit',
    'rectification:plan:extension',
    'rectification:plan:longTerm',
    'rectification:report:query',
    'rectification:report:add',
    'rectification:report:submit',
    'rectification:closure:apply'
  );
