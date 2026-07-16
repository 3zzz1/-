-- 将历史分办时间轴中的用户ID替换为真实姓名
UPDATE rect_progress p
JOIN sys_user u
  ON u.user_id = CAST(SUBSTRING_INDEX(p.content, '用户ID：', -1) AS UNSIGNED)
SET p.content = CONCAT(
  '整改任务已分办给整改执行人：',
  COALESCE(NULLIF(u.nick_name, ''), u.user_name)
)
WHERE p.progress_type = 'ASSIGN'
  AND p.content LIKE '%用户ID：%';
