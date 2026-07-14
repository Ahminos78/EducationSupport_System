USE edu_platform;
SET NAMES utf8mb4;

-- 重新分配课程到 t1~t6 教师
UPDATE tb_course SET teacher_id = 101 WHERE id BETWEEN 9101 AND 9105;
UPDATE tb_course SET teacher_id = 102 WHERE id BETWEEN 9106 AND 9110;
UPDATE tb_course SET teacher_id = 103 WHERE id IN (9201, 9202, 9203);
UPDATE tb_course SET teacher_id = 104 WHERE id IN (9204, 9205, 9206);
UPDATE tb_course SET teacher_id = 105 WHERE id BETWEEN 9301 AND 9305;
UPDATE tb_course SET teacher_id = 106 WHERE id BETWEEN 9401 AND 9405;

-- 重新分配考试到对应教师
-- t1(101) 的课程 9101-9105 下的考试
UPDATE tb_exam SET teacher_id = 101 WHERE id IN (1, 2, 3, 5, 6);
-- t3(103) 的课程 9201-9203 下的考试
UPDATE tb_exam SET teacher_id = 103 WHERE id IN (4, 7, 8);
