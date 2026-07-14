USE edu_platform;
SET NAMES utf8mb4;
UPDATE tb_user SET nickname = '张教授' WHERE id = 1000;
UPDATE tb_user SET nickname = '李教授' WHERE id = 1001;
UPDATE tb_teacher_profile SET real_name = '张教授' WHERE user_id = 1000;
UPDATE tb_teacher_profile SET real_name = '李教授' WHERE user_id = 1001;
