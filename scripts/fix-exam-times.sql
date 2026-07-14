USE edu_platform;
SET NAMES utf8mb4;

UPDATE tb_exam SET
  start_time = '2026-07-01 09:00:00',
  end_time = '2026-12-31 23:59:59'
WHERE id = 1;
