USE edu_platform;

-- 1. 新建一对一档案表。账号认证信息仍保留在 tb_user。
CREATE TABLE IF NOT EXISTS tb_student_profile (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID，对应 tb_user.id',
    student_no VARCHAR(32) NOT NULL COMMENT '学号',
    real_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    college VARCHAR(100) NULL COMMENT '学院',
    major VARCHAR(100) NULL COMMENT '专业',
    class_name VARCHAR(100) NULL COMMENT '班级',
    grade VARCHAR(20) NULL COMMENT '年级',
    enrollment_year SMALLINT NULL COMMENT '入学年份',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_profile_student_no (student_no),
    CONSTRAINT fk_student_profile_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

CREATE TABLE IF NOT EXISTS tb_teacher_profile (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID，对应 tb_user.id',
    employee_no VARCHAR(32) NOT NULL COMMENT '教师工号',
    real_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    college VARCHAR(100) NULL COMMENT '学院或部门',
    title VARCHAR(50) NULL COMMENT '职称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_profile_employee_no (employee_no),
    CONSTRAINT fk_teacher_profile_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师档案表';

-- 2. 用当前用户数据回填基础档案。后续可在 DataGrip 中补充学院、专业、班级等资料。
INSERT INTO tb_student_profile (user_id, student_no, real_name)
SELECT
    id,
    CASE
        WHEN username REGEXP '^[0-9]+$' THEN username
        ELSE CONCAT('S', LPAD(id, 8, '0'))
    END,
    nickname
FROM tb_user
WHERE role = 1 AND deleted = 0
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name);

INSERT INTO tb_teacher_profile (user_id, employee_no, real_name)
SELECT
    id,
    CASE
        WHEN username REGEXP '^[0-9]+$' THEN username
        ELSE CONCAT('T', LPAD(id, 4, '0'))
    END,
    nickname
FROM tb_user
WHERE role = 2 AND deleted = 0
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name);

-- 3. 添加外键前先检查孤儿数据。下面任意查询有结果时，请先修复数据再执行第 4 部分。
SELECT 'tb_course.teacher_id -> tb_user.id' AS relation_name, course.id AS row_id, course.teacher_id AS missing_id
FROM tb_course course LEFT JOIN tb_user user_record ON user_record.id = course.teacher_id
WHERE user_record.id IS NULL;

SELECT 'tb_enrollment.course_id -> tb_course.id' AS relation_name, enrollment.id AS row_id, enrollment.course_id AS missing_id
FROM tb_enrollment enrollment LEFT JOIN tb_course course ON course.id = enrollment.course_id
WHERE course.id IS NULL;

SELECT 'tb_enrollment.student_id -> tb_user.id' AS relation_name, enrollment.id AS row_id, enrollment.student_id AS missing_id
FROM tb_enrollment enrollment LEFT JOIN tb_user user_record ON user_record.id = enrollment.student_id
WHERE user_record.id IS NULL;

SELECT 'tb_assignment.course_id -> tb_course.id' AS relation_name, assignment.id AS row_id, assignment.course_id AS missing_id
FROM tb_assignment assignment LEFT JOIN tb_course course ON course.id = assignment.course_id
WHERE course.id IS NULL;

SELECT 'tb_assignment.teacher_id -> tb_user.id' AS relation_name, assignment.id AS row_id, assignment.teacher_id AS missing_id
FROM tb_assignment assignment LEFT JOIN tb_user user_record ON user_record.id = assignment.teacher_id
WHERE user_record.id IS NULL;

SELECT 'tb_submission.assignment_id -> tb_assignment.id' AS relation_name, submission.id AS row_id, submission.assignment_id AS missing_id
FROM tb_submission submission LEFT JOIN tb_assignment assignment ON assignment.id = submission.assignment_id
WHERE assignment.id IS NULL;

SELECT 'tb_submission.student_id -> tb_user.id' AS relation_name, submission.id AS row_id, submission.student_id AS missing_id
FROM tb_submission submission LEFT JOIN tb_user user_record ON user_record.id = submission.student_id
WHERE user_record.id IS NULL;

SELECT 'tb_exam.course_id -> tb_course.id' AS relation_name, exam.id AS row_id, exam.course_id AS missing_id
FROM tb_exam exam LEFT JOIN tb_course course ON course.id = exam.course_id
WHERE course.id IS NULL;

SELECT 'tb_exam.teacher_id -> tb_user.id' AS relation_name, exam.id AS row_id, exam.teacher_id AS missing_id
FROM tb_exam exam LEFT JOIN tb_user user_record ON user_record.id = exam.teacher_id
WHERE user_record.id IS NULL;


-- 4. 确认上述检查均返回空结果后执行本段。过程会跳过已经存在的同名外键。
DELIMITER //

DROP PROCEDURE IF EXISTS add_fk_if_missing//
CREATE PROCEDURE add_fk_if_missing(
    IN table_name_value VARCHAR(64),
    IN constraint_name_value VARCHAR(64),
    IN ddl_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_value
          AND CONSTRAINT_NAME = constraint_name_value
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @ddl_statement = ddl_value;
        PREPARE prepared_statement FROM @ddl_statement;
        EXECUTE prepared_statement;
        DEALLOCATE PREPARE prepared_statement;
    END IF;
END//

DELIMITER ;

CALL add_fk_if_missing(
    'tb_course',
    'fk_course_teacher',
    'ALTER TABLE tb_course ADD CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL add_fk_if_missing(
    'tb_enrollment',
    'fk_enrollment_course',
    'ALTER TABLE tb_enrollment ADD CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES tb_course(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);
CALL add_fk_if_missing(
    'tb_enrollment',
    'fk_enrollment_student',
    'ALTER TABLE tb_enrollment ADD CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES tb_user(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL add_fk_if_missing(
    'tb_assignment',
    'fk_assignment_course',
    'ALTER TABLE tb_assignment ADD CONSTRAINT fk_assignment_course FOREIGN KEY (course_id) REFERENCES tb_course(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);
CALL add_fk_if_missing(
    'tb_assignment',
    'fk_assignment_teacher',
    'ALTER TABLE tb_assignment ADD CONSTRAINT fk_assignment_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL add_fk_if_missing(
    'tb_submission',
    'fk_submission_assignment',
    'ALTER TABLE tb_submission ADD CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES tb_assignment(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);
CALL add_fk_if_missing(
    'tb_submission',
    'fk_submission_student',
    'ALTER TABLE tb_submission ADD CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES tb_user(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL add_fk_if_missing(
    'tb_exam',
    'fk_exam_course',
    'ALTER TABLE tb_exam ADD CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES tb_course(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);
CALL add_fk_if_missing(
    'tb_exam',
    'fk_exam_teacher',
    'ALTER TABLE tb_exam ADD CONSTRAINT fk_exam_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL add_fk_if_missing(
CALL add_fk_if_missing(
CALL add_fk_if_missing(

DROP PROCEDURE add_fk_if_missing;
