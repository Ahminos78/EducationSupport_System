USE edu_platform;

-- ============================================================
-- 第二批迁移：tb_course_schedule 建表 + tb_enrollment 改结构
-- 对应 init.sql 在行 134/153 处的 DDL 变化
-- ============================================================

-- ── 1. tb_course_schedule 建表 ──────────────────────────────
CREATE TABLE IF NOT EXISTS tb_course_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '排课ID',
    class_id BIGINT NOT NULL COMMENT '教学班ID，对应 tb_course_class.id',
    day_of_week TINYINT NOT NULL COMMENT '星期：1=周一~7=周日',
    start_period TINYINT NOT NULL COMMENT '开始节次',
    end_period TINYINT NOT NULL COMMENT '结束节次',
    start_week TINYINT NOT NULL COMMENT '起始教学周',
    end_week TINYINT NOT NULL COMMENT '结束教学周',
    week_type TINYINT NOT NULL DEFAULT 0 COMMENT '周类型：0=全周，1=单周，2=双周',
    location VARCHAR(100) COMMENT '上课地点',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_schedule_class (class_id),
    INDEX idx_schedule_weekday (day_of_week, start_period),
    CONSTRAINT fk_schedule_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学班排课表';

-- ── 2. tb_enrollment 扩展 ────────────────────────────────────

-- 2a. 为已有课程创建默认教学班（幂等）
INSERT INTO tb_course_class (course_id, teacher_id, name, max_students, enrolled_count)
SELECT c.id, c.teacher_id, CONCAT(c.name, ' 默认班'), c.max_students, c.enrolled_count
FROM tb_course c
WHERE c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM tb_course_class cc WHERE cc.course_id = c.id)
ORDER BY c.id;

-- 2b. 给 tb_enrollment 加列
DROP PROCEDURE IF EXISTS add_enrollment_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_enrollment_column_if_missing(
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_enrollment'
          AND COLUMN_NAME = target_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE tb_enrollment ADD COLUMN `', target_column, '` ', column_definition);
        PREPARE statement_to_run FROM @ddl;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

CALL add_enrollment_column_if_missing(
    'class_id',
    'BIGINT NULL COMMENT ''教学班ID，对应 tb_course_class.id'' AFTER `id`'
);
CALL add_enrollment_column_if_missing(
    'final_score',
    'DECIMAL(5,2) NULL COMMENT ''最终成绩'' AFTER `status`'
);
CALL add_enrollment_column_if_missing(
    'grade_letter',
    'VARCHAR(2) NULL COMMENT ''等第：A/B/C/D/F'' AFTER `final_score`'
);
CALL add_enrollment_column_if_missing(
    'passed',
    'TINYINT NULL COMMENT ''是否通过：0=未通过，1=通过'' AFTER `grade_letter`'
);

DROP PROCEDURE IF EXISTS add_enrollment_column_if_missing;

-- 2c. 回填 class_id
UPDATE tb_enrollment e
JOIN tb_course_class cc ON cc.course_id = e.course_id
SET e.class_id = cc.id
WHERE e.class_id IS NULL;

-- 2d. 确保 class_id 非空后加约束
ALTER TABLE tb_enrollment MODIFY COLUMN class_id BIGINT NOT NULL COMMENT '教学班ID';

-- 2e. 删除旧约束，添加新约束
-- 先删除旧 UNIQUE KEY、旧 FK、旧 INDEX
ALTER TABLE tb_enrollment
    DROP INDEX IF EXISTS uk_enrollment_course_student,
    DROP INDEX IF EXISTS idx_enrollment_course_status,
    DROP FOREIGN KEY IF EXISTS fk_enrollment_course;

-- 添加新的唯一约束：同教学班内不能重复选课
-- 注意：加 UNIQUE 前需要先清理可能的重复数据
-- 删除同教学班内的重复选课后添加约束
DELETE e1 FROM tb_enrollment e1
INNER JOIN tb_enrollment e2
WHERE e1.id > e2.id
  AND e1.class_id = e2.class_id
  AND e1.student_id = e2.student_id
  AND e1.status IN (0, 1);

-- 删除同课程内的重复选课后添加约束
DELETE e1 FROM tb_enrollment e1
INNER JOIN tb_enrollment e2
WHERE e1.id > e2.id
  AND e1.course_id = e2.course_id
  AND e1.student_id = e2.student_id
  AND e1.status IN (0, 1);

ALTER TABLE tb_enrollment
    ADD UNIQUE KEY uk_enrollment_class_student (class_id, student_id),
    ADD UNIQUE KEY uk_enrollment_course_student (course_id, student_id),
    ADD INDEX idx_enrollment_class_status (class_id, status),
    ADD CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    ADD CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT;

