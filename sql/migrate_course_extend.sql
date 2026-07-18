USE edu_platform;

-- ============================================================
-- tb_course 扩展：添加学年、学期、总学时
-- 对应 init.sql 中的 DDL 变化
-- ============================================================

DROP PROCEDURE IF EXISTS add_course_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_course_column_if_missing(
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_course'
          AND COLUMN_NAME = target_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE tb_course ADD COLUMN `', target_column, '` ', column_definition);
        PREPARE statement_to_run FROM @ddl;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

CALL add_course_column_if_missing(
    'academic_year',
    'VARCHAR(9) NULL COMMENT ''学年，如 2025-2026'' AFTER `class_count`'
);
CALL add_course_column_if_missing(
    'semester',
    'TINYINT NULL COMMENT ''开课学期：1=上学期，2=下学期，3=短学期'' AFTER `academic_year`'
);
CALL add_course_column_if_missing(
    'total_hours',
    'SMALLINT NULL COMMENT ''总学时'' AFTER `semester`'
);

DROP PROCEDURE IF EXISTS add_course_column_if_missing;

-- 回填已有课程数据
UPDATE tb_course SET
    academic_year = '2025-2026',
    semester = 2,
    total_hours = CASE id
        WHEN 9101 THEN 48  WHEN 9102 THEN 56  WHEN 9103 THEN 32
        WHEN 9104 THEN 48  WHEN 9105 THEN 64  WHEN 9106 THEN 56
        WHEN 9107 THEN 56  WHEN 9108 THEN 48  WHEN 9109 THEN 48
        WHEN 9110 THEN 48
        WHEN 9201 THEN 32  WHEN 9202 THEN 32  WHEN 9203 THEN 40
        WHEN 9204 THEN 40  WHEN 9205 THEN 40  WHEN 9206 THEN 40
        WHEN 9301 THEN 32  WHEN 9302 THEN 24  WHEN 9303 THEN 48
        WHEN 9304 THEN 32  WHEN 9305 THEN 32
        WHEN 9401 THEN 48  WHEN 9402 THEN 64  WHEN 9403 THEN 48
        WHEN 9404 THEN 48  WHEN 9405 THEN 128
        ELSE total_hours
    END
WHERE academic_year IS NULL;

