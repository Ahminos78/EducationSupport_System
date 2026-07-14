USE edu_platform;

-- This migration is for an existing database. init.sql contains the same final structure
-- for newly created databases. The helper keeps this script safe to run more than once.
DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = target_table
          AND COLUMN_NAME = target_column
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', target_table, '` ADD COLUMN `', target_column, '` ', column_definition
        );
        PREPARE statement_to_run FROM @ddl;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing(
    'tb_assignment',
    'start_time',
    'DATETIME NULL COMMENT ''学生可开始作业的时间'' AFTER `full_score`'
);
CALL add_column_if_missing(
    'tb_assignment',
    'published_at',
    'DATETIME NULL COMMENT ''正式发布时间，草稿可为空'' AFTER `status`'
);
CALL add_column_if_missing(
    'tb_submission',
    'status',
    'TINYINT NOT NULL DEFAULT 1 COMMENT ''提交状态：0=暂存，1=已提交，2=已撤回'' AFTER `attachment_url`'
);
CALL add_column_if_missing(
    'tb_submission',
    'grading_status',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''批改状态：0=待批改，1=已批改'' AFTER `status`'
);

-- Backfill old rows before making assignment start_time mandatory.
UPDATE tb_assignment
SET start_time = COALESCE(start_time, created_at),
    published_at = CASE
        WHEN status IN (1, 2) THEN COALESCE(published_at, created_at)
        ELSE published_at
    END;

ALTER TABLE tb_assignment
    MODIFY COLUMN start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学生可开始作业的时间';

UPDATE tb_submission
SET grading_status = CASE WHEN score IS NOT NULL OR graded_at IS NOT NULL THEN 1 ELSE 0 END;

-- Ensure student_liu is an approved member of both test courses.
INSERT INTO tb_enrollment (
    course_id, student_id, status, apply_reason, review_comment, applied_at, reviewed_at
)
SELECT
    course.id,
    student.id,
    1,
    '作业功能测试',
    '测试数据：审核通过',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tb_course course
JOIN tb_user student
    ON student.username = 'student_liu'
    AND student.role = 1
    AND student.deleted = 0
WHERE course.id IN (9201, 9301)
  AND course.deleted = 0
ON DUPLICATE KEY UPDATE
    status = 1,
    review_comment = '测试数据：审核通过',
    reviewed_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP;

-- Test assignment 1: Python data analysis, currently in progress.
INSERT INTO tb_assignment (
    id, course_id, teacher_id, title, description, full_score,
    start_time, deadline, status, published_at, created_at, updated_at, deleted
)
SELECT
    2026071401,
    course.id,
    course.teacher_id,
    'Python 数据分析：Pandas 数据清洗实践',
    '下载课程提供的销售数据集，完成缺失值处理、重复数据清理、字段类型转换，并提交一份包含关键代码、处理说明和结果截图的分析报告。',
    100,
    '2026-07-10 08:00:00',
    '2026-07-25 23:59:59',
    1,
    '2026-07-09 10:00:00',
    '2026-07-09 10:00:00',
    '2026-07-09 10:00:00',
    0
FROM tb_course course
WHERE course.id = 9201 AND course.deleted = 0
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    teacher_id = VALUES(teacher_id),
    title = VALUES(title),
    description = VALUES(description),
    full_score = VALUES(full_score),
    start_time = VALUES(start_time),
    deadline = VALUES(deadline),
    status = VALUES(status),
    published_at = VALUES(published_at),
    deleted = 0;

-- Test assignment 2: Mental health education, scheduled but not started.
INSERT INTO tb_assignment (
    id, course_id, teacher_id, title, description, full_score,
    start_time, deadline, status, published_at, created_at, updated_at, deleted
)
SELECT
    2026071402,
    course.id,
    course.teacher_id,
    '大学生心理健康教育：个人压力管理计划',
    '结合课程中的压力识别方法，记录一周内主要压力事件，分析情绪与行为反应，并制定一份可执行的个人压力管理计划。内容不少于 800 字。',
    100,
    '2026-07-20 08:00:00',
    '2026-08-03 23:59:59',
    1,
    '2026-07-14 09:00:00',
    '2026-07-14 09:00:00',
    '2026-07-14 09:00:00',
    0
FROM tb_course course
WHERE course.id = 9301 AND course.deleted = 0
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    teacher_id = VALUES(teacher_id),
    title = VALUES(title),
    description = VALUES(description),
    full_score = VALUES(full_score),
    start_time = VALUES(start_time),
    deadline = VALUES(deadline),
    status = VALUES(status),
    published_at = VALUES(published_at),
    deleted = 0;

DROP PROCEDURE IF EXISTS add_column_if_missing;

SELECT
    assignment.id,
    course.name AS course_name,
    user_record.nickname AS teacher_name,
    assignment.title,
    assignment.start_time,
    assignment.deadline,
    assignment.status,
    assignment.published_at
FROM tb_assignment assignment
JOIN tb_course course ON course.id = assignment.course_id
JOIN tb_user user_record ON user_record.id = assignment.teacher_id
WHERE assignment.id IN (2026071401, 2026071402)
ORDER BY assignment.id;
