-- 为 tb_user 表添加个人中心相关字段
ALTER TABLE tb_user
    ADD COLUMN email VARCHAR(100) NULL COMMENT '邮箱' AFTER role,
    ADD COLUMN phone VARCHAR(20) NULL COMMENT '手机号' AFTER email,
    ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '头像地址' AFTER phone;

-- 为已有用户自动生成合理的测试数据
UPDATE tb_user SET
    email = CASE
        WHEN username = 'admin' THEN 'admin@edu.com'
        WHEN username LIKE 't%' THEN CONCAT('teacher', username, '@edu.com')
        WHEN username LIKE 's%' THEN CONCAT('student', username, '@edu.com')
        ELSE CONCAT(username, '@edu.com')
    END,
    phone = CASE
        WHEN id = 1 THEN '13800000001'
        ELSE CONCAT('138', LPAD(CAST(id AS CHAR), 8, '0'))
    END,
    avatar_url = NULL
WHERE deleted = 0;
