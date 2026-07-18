USE edu_platform;

-- ============================================================
-- 清理过时表（2026-07-18）
-- tb_course_selection  → tb_enrollment 的前身，无数据、无代码引用
-- tb_forum_post        → 旧论坛表，无数据、无代码引用
-- tb_forum_reply       → 旧论坛表，无数据、无代码引用
-- tb_discussion        → 讨论功能，当前阶段阉割，连带移除整个 edu-interaction-service
-- ============================================================

-- 1）先删除 tb_discussion 的外键

-- 2）删除表
DROP TABLE IF EXISTS tb_discussion;
DROP TABLE IF EXISTS tb_course_selection;
DROP TABLE IF EXISTS tb_forum_post;
DROP TABLE IF EXISTS tb_forum_reply;
