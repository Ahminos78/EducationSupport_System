USE edu_platform;
SET NAMES utf8mb4;
ALTER TABLE tb_exam ADD COLUMN max_attempts INT NOT NULL DEFAULT 1 COMMENT '允许提交次数' AFTER duration;
