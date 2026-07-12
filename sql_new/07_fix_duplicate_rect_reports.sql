-- Keep the latest active rectification report for each task and soft-delete older duplicates.
-- Run this once if /rectification/report/{taskId} ever failed with "Expected one result".
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
