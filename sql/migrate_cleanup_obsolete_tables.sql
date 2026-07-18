USE edu_platform;

-- ============================================================
-- 清理过时表（2026-07-18）
-- tb_course_selection  → tb_enrollment 的前身，无数据、无代码引用
-- tb_forum_post        → 旧论坛表，无数据、无代码引用
-- tb_forum_reply       → 旧论坛表，无数据、无代码引用
-- tb_discussion        → 讨论功能，当前阶段阉割，连带移除整个 edu-interaction-service
-- ============================================================

-- 1）先删除 tb_discussion 的外键（幂等：使用存储过程跳过不存在的约束）
DROP PROCEDURE IF EXISTS drop_fk_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_fk_if_exists(
    IN tbl VARCHAR(64),
    IN fk_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl
          AND CONSTRAINT_NAME = fk_name
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @drop_sql = CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk_name, '`');
        PREPARE drop_stmt FROM @drop_sql;
        EXECUTE drop_stmt;
        DEALLOCATE PREPARE drop_stmt;
    END IF;
END$$
DELIMITER ;

CALL drop_fk_if_exists('tb_discussion', 'fk_discussion_parent');
CALL drop_fk_if_exists('tb_discussion', 'fk_discussion_course');
CALL drop_fk_if_exists('tb_discussion', 'fk_discussion_author');

DROP PROCEDURE IF EXISTS drop_fk_if_exists;

-- 2）删除表
DROP TABLE IF EXISTS tb_discussion;
DROP TABLE IF EXISTS tb_course_selection;
DROP TABLE IF EXISTS tb_forum_post;
DROP TABLE IF EXISTS tb_forum_reply;
