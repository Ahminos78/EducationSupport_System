CREATE DATABASE IF NOT EXISTS edu_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE edu_platform;

CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) NOT NULL COMMENT '用户昵称',
    role TINYINT NOT NULL COMMENT '1=STUDENT, 2=TEACHER, 3=ADMIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    UNIQUE KEY uk_user_username_deleted (username, deleted),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS tb_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID，对应 tb_user.id',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程简介',
    cover_url VARCHAR(255) COMMENT '课程封面地址',
    max_students INT NOT NULL DEFAULT 100 COMMENT '最大选课人数',
    enrolled_count INT NOT NULL DEFAULT 0 COMMENT '当前已选人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0=下架，1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE IF NOT EXISTS tb_enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选课记录ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=PENDING, 1=APPROVED, 2=DROPPED, 4=REJECTED',
    apply_reason VARCHAR(255) COMMENT '选课申请说明',
    review_comment VARCHAR(255) COMMENT '审核意见',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_enrollment_course_student (course_id, student_id),
    INDEX idx_enrollment_student (student_id),
    INDEX idx_enrollment_course_status (course_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

CREATE TABLE IF NOT EXISTS tb_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '发布教师ID，对应 tb_user.id',
    title VARCHAR(100) NOT NULL COMMENT '作业标题',
    description TEXT COMMENT '作业说明',
    full_score INT NOT NULL DEFAULT 100 COMMENT '满分',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '作业状态：0=草稿，1=已发布，2=已截止',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_assignment_course (course_id),
    INDEX idx_assignment_teacher (teacher_id),
    INDEX idx_assignment_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

CREATE TABLE IF NOT EXISTS tb_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业提交ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID，对应 tb_assignment.id',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    content TEXT NOT NULL COMMENT '提交内容',
    attachment_url VARCHAR(255) COMMENT '附件地址',
    score INT NULL COMMENT '得分',
    teacher_comment TEXT COMMENT '教师评语',
    ai_comment TEXT COMMENT 'AI辅助评语',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    graded_at DATETIME NULL COMMENT '批改时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_submission_assignment_student (assignment_id, student_id),
    INDEX idx_submission_student (student_id),
    INDEX idx_submission_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

CREATE TABLE IF NOT EXISTS tb_exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '发布教师ID，对应 tb_user.id',
    title VARCHAR(100) NOT NULL COMMENT '考试标题',
    description TEXT COMMENT '考试说明',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    full_score INT NOT NULL DEFAULT 100 COMMENT '满分',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '考试状态：0=草稿，1=已发布，2=已结束',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_exam_course (course_id),
    INDEX idx_exam_teacher (teacher_id),
    INDEX idx_exam_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

CREATE TABLE IF NOT EXISTS tb_discussion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '讨论ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    parent_id BIGINT NULL COMMENT '父讨论ID，NULL表示主题帖',
    author_id BIGINT NOT NULL COMMENT '作者ID，对应 tb_user.id',
    title VARCHAR(100) NULL COMMENT '主题帖标题，回复可为空',
    content TEXT NOT NULL COMMENT '讨论内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=隐藏，1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_discussion_course_parent (course_id, parent_id),
    INDEX idx_discussion_author (author_id),
    INDEX idx_discussion_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程讨论表';
