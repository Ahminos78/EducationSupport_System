USE edu_platform;

CREATE TABLE IF NOT EXISTS tb_assignment_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业附件ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID，对应 tb_assignment.id',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '服务器存储文件名',
    content_type VARCHAR(100) NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小，字节',
    uploaded_by BIGINT NOT NULL COMMENT '上传教师ID，对应 tb_user.id',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_assignment_attachment_assignment (assignment_id),
    CONSTRAINT fk_assignment_attachment_assignment FOREIGN KEY (assignment_id) REFERENCES tb_assignment(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_assignment_attachment_uploader FOREIGN KEY (uploaded_by) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师发布的作业附件表';

CREATE TABLE IF NOT EXISTS tb_submission_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生提交附件ID',
    submission_id BIGINT NOT NULL COMMENT '提交记录ID，对应 tb_submission.id',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '服务器存储文件名',
    content_type VARCHAR(100) NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小，字节',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_submission_attachment_submission (submission_id),
    CONSTRAINT fk_submission_attachment_submission FOREIGN KEY (submission_id) REFERENCES tb_submission(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生作业提交附件表';

-- Python data analysis test assignment attachment. The matching file is packaged
-- in edu-assessment-service/src/main/resources/seed-files.
INSERT INTO tb_assignment_attachment (
    id, assignment_id, original_name, stored_name,
    content_type, file_size, uploaded_by, created_at
)
SELECT
    2026071401,
    assignment.id,
    'Python高级程序设计大作业模板.doc',
    'python-advanced-homework-template.doc',
    'application/msword',
    87815,
    assignment.teacher_id,
    '2026-07-09 10:00:00'
FROM tb_assignment assignment
WHERE assignment.id = 2026071401
  AND assignment.deleted = 0
ON DUPLICATE KEY UPDATE
    assignment_id = VALUES(assignment_id),
    original_name = VALUES(original_name),
    stored_name = VALUES(stored_name),
    content_type = VALUES(content_type),
    file_size = VALUES(file_size),
    uploaded_by = VALUES(uploaded_by);
