USE edu_platform;

-- ============================================================
-- 第 7 批迁移：为关键业务表添加软删除字段
-- 
-- 涉及表：tb_course_class, tb_enrollment, tb_student_profile, tb_teacher_profile
-- 
-- 这些表在业务上需要保留已删除记录以便审计或恢复，
-- 同时已有多个 Mapper 查询引用了这些表的 deleted=0 条件。
-- 本迁移统一补上 deleted 列，配合 Java 实体上的 @TableLogic 实现软删除。
-- ============================================================

DROP PROCEDURE IF EXISTS add_deleted_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_deleted_column_if_missing(IN target_table VARCHAR(64))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = target_table
          AND COLUMN_NAME = 'deleted'
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', target_table, '` ',
            'ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 ',
            'COMMENT \'逻辑删除：0=正常，1=删除\' ',
            'AFTER `updated_at`'
        );
        PREPARE statement_to_run FROM @ddl;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

CALL add_deleted_column_if_missing('tb_course_class');
CALL add_deleted_column_if_missing('tb_enrollment');
CALL add_deleted_column_if_missing('tb_student_profile');
CALL add_deleted_column_if_missing('tb_teacher_profile');

DROP PROCEDURE IF EXISTS add_deleted_column_if_missing;
