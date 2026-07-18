USE edu_platform;

-- ============================================================
-- 第三批迁移：tb_grade_component + tb_student_grade
-- 对应 init.sql 中 tb_enrollment 之后的 DDL
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_grade_component (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成绩组成项ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    name VARCHAR(50) NOT NULL COMMENT '组成项名称，如 平时作业、期中考试、期末考试',
    weight DECIMAL(5,4) NOT NULL COMMENT '权重，如 0.3000 表示 30%',
    max_score SMALLINT NULL COMMENT '满分',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_grade_component_course (course_id),
    INDEX idx_grade_component_sort (course_id, sort_order),
    CONSTRAINT fk_grade_component_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩组成项表';

CREATE TABLE IF NOT EXISTS tb_student_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生成绩明细ID',
    enrollment_id BIGINT NOT NULL COMMENT '选课记录ID，对应 tb_enrollment.id',
    component_id BIGINT NOT NULL COMMENT '成绩组成项ID，对应 tb_grade_component.id',
    score DECIMAL(6,2) NULL COMMENT '得分',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_grade (enrollment_id, component_id),
    INDEX idx_student_grade_enrollment (enrollment_id),
    INDEX idx_student_grade_component (component_id),
    CONSTRAINT fk_student_grade_enrollment FOREIGN KEY (enrollment_id) REFERENCES tb_enrollment(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_student_grade_component FOREIGN KEY (component_id) REFERENCES tb_grade_component(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩明细表';

