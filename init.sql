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
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS tb_student_profile (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID，对应 tb_user.id',
    student_no VARCHAR(32) NOT NULL COMMENT '学号',
    real_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    college VARCHAR(100) NULL COMMENT '学院',
    major VARCHAR(100) NULL COMMENT '专业',
    class_name VARCHAR(100) NULL COMMENT '班级',
    grade VARCHAR(20) NULL COMMENT '年级',
    enrollment_year SMALLINT NULL COMMENT '入学年份',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_profile_student_no (student_no),
    CONSTRAINT fk_student_profile_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

CREATE TABLE IF NOT EXISTS tb_teacher_profile (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID，对应 tb_user.id',
    employee_no VARCHAR(32) NOT NULL COMMENT '教师工号',
    real_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    college VARCHAR(100) NULL COMMENT '学院或部门',
    title VARCHAR(50) NULL COMMENT '职称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_profile_employee_no (employee_no),
    CONSTRAINT fk_teacher_profile_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师档案表';


CREATE TABLE IF NOT EXISTS tb_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    code VARCHAR(20) COMMENT '课程号',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID，对应 tb_user.id',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程简介',
    cover_url VARCHAR(255) COMMENT '课程封面地址',
    max_students INT NOT NULL DEFAULT 100 COMMENT '最大选课人数',
    enrolled_count INT NOT NULL DEFAULT 0 COMMENT '当前已选人数',
    credit DECIMAL(4,1) COMMENT '学分',
    dept VARCHAR(50) COMMENT '开课单位',
    category VARCHAR(30) COMMENT '课程性质（必修/选修/通识/个性课程）',
    tags VARCHAR(100) COMMENT '课程标签',
    class_count INT DEFAULT 1 COMMENT '教学班个数',
    class_id BIGINT NULL COMMENT '默认教学班ID，对应 tb_course_class.id',
    academic_year VARCHAR(9) COMMENT '学年，如 2025-2026',
    semester TINYINT COMMENT '开课学期：1=上学期，2=下学期，3=短学期',
    total_hours SMALLINT COMMENT '总学时',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0=下架，1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_status (status),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';



CREATE TABLE IF NOT EXISTS tb_course_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '教学班ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID，对应 tb_user.id',
    name VARCHAR(100) NOT NULL COMMENT '教学班名称，如 计科2301班',
    max_students INT NOT NULL DEFAULT 60 COMMENT '班级容量',
    enrolled_count INT NOT NULL DEFAULT 0 COMMENT '当前已选人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=停开，1=开课',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_class_course (course_id),
    INDEX idx_course_class_teacher (teacher_id),
    CONSTRAINT fk_course_class_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_course_class_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学班表';

-- 添加默认教学班外键（幂等：如果已存在则跳过）
DROP PROCEDURE IF EXISTS add_default_class_fk_if_missing;
DELIMITER $$
CREATE PROCEDURE add_default_class_fk_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_course'
          AND CONSTRAINT_NAME = 'fk_course_default_class'
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tb_course ADD CONSTRAINT fk_course_default_class
            FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
            ON UPDATE CASCADE ON DELETE SET NULL;
    END IF;
END$$
DELIMITER ;

CALL add_default_class_fk_if_missing();
DROP PROCEDURE IF EXISTS add_default_class_fk_if_missing;

CREATE TABLE IF NOT EXISTS tb_course_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '排课ID',
    class_id BIGINT NOT NULL COMMENT '教学班ID，对应 tb_course_class.id',
    day_of_week TINYINT NOT NULL COMMENT '星期：1=周一~7=周日',
    start_period TINYINT NOT NULL COMMENT '开始节次',
    end_period TINYINT NOT NULL COMMENT '结束节次',
    start_week TINYINT NOT NULL COMMENT '起始教学周',
    end_week TINYINT NOT NULL COMMENT '结束教学周',
    week_type TINYINT NOT NULL DEFAULT 0 COMMENT '周类型：0=全周，1=单周，2=双周',
    location VARCHAR(100) COMMENT '上课地点',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_schedule_class (class_id),
    INDEX idx_schedule_weekday (day_of_week, start_period),
    CONSTRAINT fk_schedule_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学班排课表';


CREATE TABLE IF NOT EXISTS tb_enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选课记录ID',
    class_id BIGINT NOT NULL COMMENT '教学班ID，对应 tb_course_class.id',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    course_id BIGINT NOT NULL COMMENT '课程ID（冗余，便于约束和查询）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=PENDING, 1=APPROVED, 2=DROPPED, 4=REJECTED',
    final_score DECIMAL(5,2) NULL COMMENT '最终成绩',
    grade_letter VARCHAR(2) NULL COMMENT '等第：A/B/C/D/F',
    passed TINYINT NULL COMMENT '是否通过：0=未通过，1=通过',
    apply_reason VARCHAR(255) COMMENT '选课申请说明',
    review_comment VARCHAR(255) COMMENT '审核意见',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_enrollment_class_student (class_id, student_id),
    UNIQUE KEY uk_enrollment_course_student (course_id, student_id),
    INDEX idx_enrollment_student (student_id),
    INDEX idx_enrollment_class_status (class_id, status),
    CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

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

CREATE TABLE IF NOT EXISTS tb_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '发布教师ID，对应 tb_user.id',
    title VARCHAR(100) NOT NULL COMMENT '作业标题',
    description TEXT COMMENT '作业说明',
    full_score INT NOT NULL DEFAULT 100 COMMENT '满分',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学生可开始作业的时间',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    allow_late_submission TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许延期提交：0=不允许，1=允许',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '作业状态：0=草稿，1=已发布，2=已截止',
    published_at DATETIME NULL COMMENT '正式发布时间，草稿可为空',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_assignment_course (course_id),
    INDEX idx_assignment_teacher (teacher_id),
    INDEX idx_assignment_time (start_time, deadline),
    CONSTRAINT fk_assignment_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_assignment_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

CREATE TABLE IF NOT EXISTS tb_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业提交ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID，对应 tb_assignment.id',
    student_id BIGINT NOT NULL COMMENT '学生ID，对应 tb_user.id',
    content TEXT NOT NULL COMMENT '提交内容',
    attachment_url VARCHAR(255) COMMENT '附件地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '提交状态：0=暂存，1=已提交，2=已撤回',
    grading_status TINYINT NOT NULL DEFAULT 0 COMMENT '批改状态：0=待批改，1=已批改',
    score INT NULL COMMENT '得分',
    teacher_comment TEXT COMMENT '教师评语',
    ai_comment TEXT COMMENT 'AI辅助评语',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    graded_at DATETIME NULL COMMENT '批改时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_submission_assignment_student (assignment_id, student_id),
    INDEX idx_submission_student (student_id),
    INDEX idx_submission_score (score),
    CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES tb_assignment(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

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

CREATE TABLE IF NOT EXISTS tb_exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
    course_id BIGINT NOT NULL COMMENT '课程ID，对应 tb_course.id',
    teacher_id BIGINT NOT NULL COMMENT '发布教师ID，对应 tb_user.id',
    title VARCHAR(100) NOT NULL COMMENT '考试标题',
    description TEXT COMMENT '考试说明',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    full_score INT NOT NULL DEFAULT 100 COMMENT '满分',
    duration INT NOT NULL DEFAULT 60 COMMENT '考试时长（分钟）',
    max_attempts INT NOT NULL DEFAULT 1 COMMENT '允许提交次数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '考试状态：0=草稿，1=已发布，2=已结束',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_exam_course (course_id),
    INDEX idx_exam_teacher (teacher_id),
    INDEX idx_exam_time (start_time, end_time),
    CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_exam_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

CREATE TABLE IF NOT EXISTS tb_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID，对应 tb_exam.id',
    type TINYINT NOT NULL COMMENT '题型：0=选择题，1=填空题',
    title TEXT NOT NULL COMMENT '题目内容',
    options TEXT NULL COMMENT '选择题选项，JSON 数组',
    answer TEXT NULL COMMENT '正确答案',
    score INT NOT NULL DEFAULT 10 COMMENT '题目分值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '题目顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_question_exam_sort (exam_id, sort_order),
    CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES tb_exam(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试题目表';

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

CREATE TABLE IF NOT EXISTS kb_knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '知识库ID',
    name VARCHAR(200) NOT NULL COMMENT '知识库名称',
    description VARCHAR(500) COMMENT '知识库描述',
    collection_name VARCHAR(100) COMMENT 'Chroma 集合名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=停用，1=正常',
    created_by BIGINT COMMENT '创建人用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_kb_status (status),
    CONSTRAINT fk_kb_created_by FOREIGN KEY (created_by) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    kb_id BIGINT NOT NULL COMMENT '所属知识库ID',
    file_name VARCHAR(255) COMMENT '服务器存储文件名',
    original_name VARCHAR(500) NOT NULL COMMENT '原始文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(20) COMMENT '文件类型（pdf/md/txt等）',
    chunk_count INT NOT NULL DEFAULT 0 COMMENT '切分后的文本块数量',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0=处理中，1=成功，2=失败',
    error_msg VARCHAR(1000) COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_kb_doc_kb (kb_id),
    INDEX idx_kb_doc_status (status),
    CONSTRAINT fk_kb_doc_kb FOREIGN KEY (kb_id) REFERENCES kb_knowledge_base(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';
