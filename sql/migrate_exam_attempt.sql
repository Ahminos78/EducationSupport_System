USE edu_platform;

-- Existing databases use this migration; init.sql remains the full schema for new databases.
CREATE TABLE IF NOT EXISTS tb_exam_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试参与记录ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID，对应 tb_exam.id',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    answer_content LONGTEXT NULL COMMENT '考试作答内容，后续可改为结构化答题明细',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '参与状态：0=进行中，1=已提交，2=已批改',
    score INT NULL COMMENT '考试得分',
    teacher_comment TEXT NULL COMMENT '教师评语',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始考试时间',
    submitted_at DATETIME NULL COMMENT '交卷时间',
    graded_at DATETIME NULL COMMENT '批改时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_exam_attempt_exam_student (exam_id, student_id),
    INDEX idx_exam_attempt_student (student_id),
    INDEX idx_exam_attempt_status (exam_id, status),
    CONSTRAINT fk_exam_attempt_exam FOREIGN KEY (exam_id) REFERENCES tb_exam(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_exam_attempt_student FOREIGN KEY (student_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生考试参与记录表';
