-- 审计整改历史数据修复，可在已有数据库重复执行

-- 1. 同一任务只保留最新一份有效整改报告。
-- 用于修复 selectOne() 返回多条整改报告的问题。
UPDATE rect_report r
JOIN (
    SELECT r1.report_id
    FROM rect_report r1
    JOIN rect_report r2
      ON r1.task_id = r2.task_id
     AND r1.del_flag = '0'
     AND r2.del_flag = '0'
     AND (
          COALESCE(r1.update_time, r1.create_time, '1970-01-01') < COALESCE(r2.update_time, r2.create_time, '1970-01-01')
          OR (
              COALESCE(r1.update_time, r1.create_time, '1970-01-01') = COALESCE(r2.update_time, r2.create_time, '1970-01-01')
              AND r1.report_id < r2.report_id
          )
     )
) dup ON r.report_id = dup.report_id
SET r.del_flag = '2',
    r.update_time = NOW(),
    r.remark = CONCAT(COALESCE(NULLIF(r.remark, ''), ''), ' duplicate report archived');

-- 2. 将历史分办时间轴中的用户ID替换为真实姓名。
UPDATE rect_progress p
JOIN sys_user u
  ON u.user_id = CAST(SUBSTRING_INDEX(p.content, '用户ID：', -1) AS UNSIGNED)
SET p.content = CONCAT(
  '整改任务已分办给整改执行人：',
  COALESCE(NULLIF(u.nick_name, ''), u.user_name)
)
WHERE p.progress_type = 'ASSIGN'
  AND p.content LIKE '%用户ID：%';
