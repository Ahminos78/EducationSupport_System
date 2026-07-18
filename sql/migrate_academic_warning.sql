USE edu_platform;

-- ============================================================
-- 第四批迁移：tb_academic_warning 学业预警表
-- 对应 init.sql 末尾的 DDL
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_academic_warning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预警记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    enrollment_id BIGINT NULL COMMENT '选课记录ID，对应 tb_enrollment.id',
    warning_type TINYINT NOT NULL COMMENT '预警类型：1=期中预警，2=期末预警，3=考勤预警',
    severity TINYINT NOT NULL DEFAULT 1 COMMENT '严重程度：1=一般，2=严重，3=非常严重',
    current_score DECIMAL(5,2) NULL COMMENT '触发预警时的成绩',
    threshold_score DECIMAL(5,2) NULL COMMENT '触发阈值',
    description VARCHAR(500) COMMENT '预警描述',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=未处理，1=已通知，2=已处理',
    processed_by BIGINT NULL COMMENT '处理人ID，对应 tb_user.id',
    process_comment VARCHAR(500) COMMENT '处理意见',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    processed_at DATETIME NULL COMMENT '处理时间',
    INDEX idx_warning_student (student_id),
    INDEX idx_warning_status (status),
    INDEX idx_warning_created (created_at),
    CONSTRAINT fk_warning_student FOREIGN KEY (student_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_warning_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_warning_enrollment FOREIGN KEY (enrollment_id) REFERENCES tb_enrollment(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_warning_processor FOREIGN KEY (processed_by) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学业预警表';
