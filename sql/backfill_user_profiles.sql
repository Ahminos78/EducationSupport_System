USE edu_platform;

START TRANSACTION;

-- 档案迁移脚本会先为角色用户创建档案；这里补齐现有三名测试学生的信息。
INSERT INTO tb_student_profile (
    user_id, student_no, real_name, college, major, class_name, grade
)
SELECT
    user_record.id,
    CASE
        WHEN user_record.username REGEXP '^[0-9]+$' THEN user_record.username
        ELSE CONCAT('S', LPAD(user_record.id, 8, '0'))
    END,
    user_record.nickname,
    '计算机智能学院',
    '软件工程',
    '软件2301',
    '大三'
FROM tb_user user_record
WHERE user_record.username IN ('student_zhang', 'student_chen', 'student_liu')
  AND user_record.role = 1
  AND user_record.deleted = 0
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    college = VALUES(college),
    major = VALUES(major),
    class_name = VALUES(class_name),
    grade = VALUES(grade);

-- 确保后续新增或已存在的教师账号都有一对一教师档案。
INSERT INTO tb_teacher_profile (user_id, employee_no, real_name, college)
SELECT
    user_record.id,
    CASE
        WHEN user_record.username REGEXP '^[0-9]+$' THEN user_record.username
        ELSE CONCAT('T', LPAD(user_record.id, 4, '0'))
    END,
    user_record.nickname,
    '计算机智能学院'
FROM tb_user user_record
WHERE user_record.role = 2
  AND user_record.deleted = 0
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    college = CASE
        WHEN tb_teacher_profile.college IS NULL OR TRIM(tb_teacher_profile.college) = ''
            THEN VALUES(college)
        ELSE tb_teacher_profile.college
    END;

COMMIT;

SELECT user_id, student_no, real_name, college, major, class_name, grade
FROM tb_student_profile
WHERE user_id IN (
    SELECT id
    FROM tb_user
    WHERE username IN ('student_zhang', 'student_chen', 'student_liu')
)
ORDER BY user_id;

SELECT user_id, employee_no, real_name, college, title
FROM tb_teacher_profile
ORDER BY user_id;
