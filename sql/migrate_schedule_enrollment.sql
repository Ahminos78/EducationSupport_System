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

-- 2a. 给 tb_enrollment 加列
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

-- 2b. 回填 class_id
UPDATE tb_enrollment e
JOIN tb_course_class cc ON cc.course_id = e.course_id
SET e.class_id = cc.id
WHERE e.class_id IS NULL;

-- 2c. 确保 class_id 非空后加约束
ALTER TABLE tb_enrollment MODIFY COLUMN class_id BIGINT NOT NULL COMMENT '教学班ID';

-- 2d. 删除旧约束和旧索引（幂等：先检查是否存在）
DROP PROCEDURE IF EXISTS drop_key_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_key_if_exists(tbl VARCHAR(64), key_name VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl
          AND INDEX_NAME = key_name
    ) THEN
        SET @drop_sql = CONCAT('ALTER TABLE `', tbl, '` DROP INDEX `', key_name, '`');
        PREPARE stmt FROM @drop_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS drop_fk_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_fk_if_exists(tbl VARCHAR(64), fk_name VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl
          AND CONSTRAINT_NAME = fk_name
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @drop_sql = CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk_name, '`');
        PREPARE stmt FROM @drop_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 必须按 FK → INDEX 的顺序删除，因为 fk_enrollment_course 依赖 idx_enrollment_course_status
CALL drop_fk_if_exists('tb_enrollment', 'fk_enrollment_course');
CALL drop_key_if_exists('tb_enrollment', 'uk_enrollment_course_student');
CALL drop_key_if_exists('tb_enrollment', 'idx_enrollment_course_status');

DROP PROCEDURE IF EXISTS drop_key_if_exists;
DROP PROCEDURE IF EXISTS drop_fk_if_exists;

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

