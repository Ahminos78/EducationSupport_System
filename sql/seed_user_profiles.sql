USE edu_platform;

-- ============================================================
-- 用户与档案种子数据
-- 从以下文件合并：
--   fix-charset-data.sql / fix-teacher-names.sql / backfill_user_profiles.sql
--   seed_development_teachers_and_courses.sql（用户部分）
-- 所有 seed 密码哈希对应明文：123456
-- 快速登录：t1~t6 / s1~s15 / admin
-- ============================================================

-- ── 1. 管理员账号 ────────────────────────────────────────────
INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (1, 'admin', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '系统管理员', 3, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

-- ── 2. 教师账号（t1~t6）──────────────────────────────────────
INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (101, 't1', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '张老师', 2, 0),
    (102, 't2', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '李老师', 2, 0),
    (103, 't3', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '王老师', 2, 0),
    (104, 't4', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '赵老师', 2, 0),
    (105, 't5', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '刘老师', 2, 0),
    (106, 't6', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '陈老师', 2, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

-- init.sql 提供的 teacher01/02（确保名称、密码与快速登录一致）
INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (1000, 'teacher01', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '张教授', 2, 0),
    (1001, 'teacher02', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '李教授', 2, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

-- ── 3. 学生账号（s1~s15）─────────────────────────────────────
INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (201, 's1', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '张同学', 1, 0),
    (202, 's2', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '李同学', 1, 0),
    (203, 's3', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '王同学', 1, 0),
    (204, 's4', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '赵同学', 1, 0),
    (205, 's5', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '刘同学', 1, 0),
    (206, 's6', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '陈同学', 1, 0),
    (207, 's7', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '杨同学', 1, 0),
    (208, 's8', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '黄同学', 1, 0),
    (209, 's9', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '周同学', 1, 0),
    (210, 's10','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '吴同学', 1, 0),
    (211, 's11','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '郑同学', 1, 0),
    (212, 's12','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '孙同学', 1, 0),
    (213, 's13','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '马同学', 1, 0),
    (214, 's14','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '胡同学', 1, 0),
    (215, 's15','$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '林同学', 1, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

-- ── 4. 教师档案（t1~t6 + teacher01/02）─────────────────────────
INSERT INTO tb_teacher_profile (user_id, employee_no, real_name, college)
VALUES
    (101, 'T0101', '张老师', '计算机科学与技术学院'),
    (102, 'T0102', '李老师', '软件工程系'),
    (103, 'T0103', '王老师', '人工智能学院'),
    (104, 'T0104', '赵老师', '计算机科学与技术学院'),
    (105, 'T0105', '刘老师', '软件工程系'),
    (106, 'T0106', '陈老师', '外国语学院')
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name), college = VALUES(college);

INSERT INTO tb_teacher_profile (user_id, employee_no, real_name, college)
VALUES
    (1000, 'T1000', '张教授', '计算机科学与技术学院'),
    (1001, 'T1001', '李教授', '软件工程系')
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name), college = VALUES(college);

-- ── 5. 学生档案（s1~s15）─────────────────────────────────────
INSERT INTO tb_student_profile (user_id, student_no, real_name, college, major, class_name, grade, enrollment_year)
VALUES
    (201, '2023001', '张同学', '计算机科学与技术学院', '计算机科学与技术', '计科2301班', '2023', 2023),
    (202, '2023002', '李同学', '计算机科学与技术学院', '软件工程', '软工2301班', '2023', 2023),
    (203, '2023003', '王同学', '软件工程系', '软件工程', '软工2302班', '2023', 2023),
    (204, '2023004', '赵同学', '人工智能学院', '人工智能', 'AI2301班', '2023', 2023),
    (205, '2023005', '刘同学', '计算机科学与技术学院', '计算机科学与技术', '计科2302班', '2023', 2023),
    (206, '2023006', '陈同学', '软件工程系', '软件工程', '软工2303班', '2023', 2023),
    (207, '2023007', '杨同学', '人工智能学院', '数据科学', '数科2301班', '2023', 2023),
    (208, '2023008', '黄同学', '计算机科学与技术学院', '计算机科学与技术', '计科2303班', '2023', 2023),
    (209, '2023009', '周同学', '软件工程系', '软件工程', '软工2304班', '2023', 2023),
    (210, '2023010', '吴同学', '计算机科学与技术学院', '网络工程', '网工2301班', '2023', 2023),
    (211, '2023011', '郑同学', '人工智能学院', '人工智能', 'AI2302班', '2023', 2023),
    (212, '2023012', '孙同学', '计算机科学与技术学院', '计算机科学与技术', '计科2304班', '2023', 2023),
    (213, '2023013', '马同学', '软件工程系', '软件工程', '软工2305班', '2023', 2023),
    (214, '2023014', '胡同学', '计算机科学与技术学院', '信息安全', '信安2301班', '2023', 2023),
    (215, '2023015', '林同学', '软件工程系', '软件工程', '软工2306班', '2023', 2023)
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name), college = VALUES(college),
    major = VALUES(major), class_name = VALUES(class_name),
    grade = VALUES(grade), enrollment_year = VALUES(enrollment_year);

-- ── 6. 验证 ──────────────────────────────────────────────────
SELECT id, username, nickname, role FROM tb_user WHERE deleted = 0 ORDER BY role, id;
SELECT user_id, employee_no, real_name, college FROM tb_teacher_profile ORDER BY user_id;
SELECT user_id, student_no, real_name, college, major, class_name FROM tb_student_profile ORDER BY user_id;
