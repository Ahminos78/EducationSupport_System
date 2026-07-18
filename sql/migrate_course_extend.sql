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

CALL add_course_column_if_missing(
    'class_id',
    'BIGINT NULL COMMENT ''默认教学班ID，对应 tb_course_class.id'' AFTER `class_count`'
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
    END
WHERE academic_year IS NULL;

-- 确保教学班表存在（existed-db 场景：先建表再回填 class_id）
CREATE TABLE IF NOT EXISTS tb_course_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '教学班ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID，对应 tb_user.id',
    name VARCHAR(100) NOT NULL COMMENT '教学班名称，如 计科2301班',
    max_students INT NOT NULL DEFAULT 60 COMMENT '班级容量',
    enrolled_count INT NOT NULL DEFAULT 0 COMMENT '当前已选人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=停开，1=开课',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_class_course (course_id),
    INDEX idx_course_class_teacher (teacher_id),
    CONSTRAINT fk_course_class_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_course_class_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学班表';

-- 为已有课程创建默认教学班（幂等）
INSERT INTO tb_course_class (course_id, teacher_id, name, max_students, enrolled_count)
SELECT c.id, c.teacher_id, CONCAT(c.name, ' 默认班'), COALESCE(c.max_students, 60), COALESCE(c.enrolled_count, 0)
FROM tb_course c
WHERE c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM tb_course_class cc WHERE cc.course_id = c.id)
ORDER BY c.id;

-- 回填默认教学班
UPDATE tb_course c
JOIN tb_course_class cc ON cc.course_id = c.id
SET c.class_id = cc.id
WHERE c.class_id IS NULL;

-- 添加默认教学班外键（幂等：如果已存在则跳过）
SET @fk_exists := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tb_course'
      AND CONSTRAINT_NAME = 'fk_course_default_class'
);
SET @sql_add_fk := IF(@fk_exists = 0,
    'ALTER TABLE tb_course ADD CONSTRAINT fk_course_default_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT "fk_course_default_class 已存在，跳过" AS msg'
);
PREPARE stmt FROM @sql_add_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


