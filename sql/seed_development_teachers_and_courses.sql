USE edu_platform;

START TRANSACTION;

-- 从 DataGrip 控制台归档的开发教师账号，密码均为 123456。
-- 若当前自增值已经超过 1000，MySQL 会保留现有的更大值。
ALTER TABLE tb_user AUTO_INCREMENT = 1000;

INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (1000, 'teacher01', '$2y$10$tfsA779NP5yI./FZRuzWJ.A2RV.qGFWWaq6MdDw6sA/rOZqkVofBa', '张教授', 2, 0),
    (1001, 'teacher02', '$2y$10$tfsA779NP5yI./FZRuzWJ.A2RV.qGFWWaq6MdDw6sA/rOZqkVofBa', '李教授', 2, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

-- 保存控制台中补建的基础课程，脚本可重复执行。
INSERT INTO tb_course (
    id, code, name, teacher_id, credit, dept, category, tags,
    class_count, enrolled_count, status, deleted
)
VALUES
    (9101, '9101', 'Java Web 开发实战', 1000, 3.0, '计算机科学与技术学院', '必修', '核心课', 2, 0, 1, 0),
    (9102, '9102', '数据库系统原理', 1000, 3.5, '计算机科学与技术学院', '必修', '核心课', 3, 0, 1, 0),
    (9103, '9103', 'MyBatis Plus 企业开发', 1000, 2.0, '软件工程系', '必修', '核心课', 2, 0, 1, 0)
ON DUPLICATE KEY UPDATE
    code = VALUES(code),
    name = VALUES(name),
    teacher_id = VALUES(teacher_id),
    credit = VALUES(credit),
    dept = VALUES(dept),
    category = VALUES(category),
    tags = VALUES(tags),
    class_count = VALUES(class_count),
    status = VALUES(status),
    deleted = VALUES(deleted);

-- 延续控制台中的课程分配规则。
UPDATE tb_course
SET teacher_id = CASE
    WHEN category = '必修' THEN 1000
    WHEN category IN ('选修', '通识', '个性课程') THEN 1001
    ELSE teacher_id
END
WHERE category IN ('必修', '选修', '通识', '个性课程');

COMMIT;

SELECT id, username, nickname, role, deleted
FROM tb_user
WHERE id IN (1000, 1001)
ORDER BY id;

SELECT id, code, name, teacher_id, category
FROM tb_course
WHERE deleted = 0
ORDER BY id;
