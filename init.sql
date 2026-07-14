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

-- 开发环境教师账号，密码均为 123456。课程测试数据依赖这两个用户。
INSERT IGNORE INTO tb_user (id, username, password_hash, nickname, role) VALUES
(1000, 'teacher01', '$2y$10$tfsA779NP5yI./FZRuzWJ.A2RV.qGFWWaq6MdDw6sA/rOZqkVofBa', '张教授', 2),
(1001, 'teacher02', '$2y$10$tfsA779NP5yI./FZRuzWJ.A2RV.qGFWWaq6MdDw6sA/rOZqkVofBa', '李教授', 2);

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

INSERT IGNORE INTO tb_teacher_profile (user_id, employee_no, real_name, college) VALUES
(1000, 'T1000', '张教授', '计算机科学与技术学院'),
(1001, 'T1001', '李教授', '软件工程系');

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
    status TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0=下架，1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_status (status),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 测试课程数据
INSERT INTO tb_course(id,code,name,teacher_id,credit,dept,category,tags,class_count,enrolled_count,status,deleted) VALUES
(9101,'9101','Java Web 开发实战',1000,3.0,'计算机科学与技术学院','必修','核心课',2,0,1,0),
(9102,'9102','数据库系统原理',1000,3.5,'计算机科学与技术学院','必修','核心课',3,0,1,0),
(9103,'9103','MyBatis Plus 企业开发',1000,2.0,'软件工程系','必修','核心课',2,0,1,0),
(9104,'9104','Spring Boot 微服务开发',1000,3.0,'软件工程系','必修','核心课',2,0,1,0),
(9105,'9105','数据结构与算法',1000,4.0,'计算机科学与技术学院','必修','核心课',4,0,1,0),
(9106,'9106','操作系统',1000,3.5,'计算机科学与技术学院','必修','核心课',3,0,1,0),
(9107,'9107','计算机网络',1000,3.5,'计算机科学与技术学院','必修','核心课',3,0,1,0),
(9108,'9108','软件工程',1000,3.0,'软件工程系','必修','核心课',3,0,1,0),
(9109,'9109','编译原理',1000,3.0,'软件工程系','必修','专业课',2,0,1,0),
(9110,'9110','Linux 系统管理',1000,2.5,'软件工程系','必修','实践课',2,0,1,0),
(9201,'9201','Python 数据分析',1001,2.0,'软件工程系','选修','专业选修',2,0,1,0),
(9202,'9202','Vue3 前端开发',1001,2.0,'软件工程系','选修','专业选修',2,0,1,0),
(9203,'9203','人工智能基础',1001,2.5,'人工智能学院','选修','AI课程',2,0,1,0),
(9204,'9204','深度学习导论',1001,2.5,'人工智能学院','选修','AI课程',1,0,1,0),
(9205,'9205','云计算与容器技术',1001,2.0,'软件工程系','选修','专业选修',2,0,1,0),
(9206,'9206','大数据技术基础',1001,2.5,'计算机科学与技术学院','选修','专业选修',2,0,1,0),
(9301,'9301','大学生心理健康教育',1001,2.0,'马克思主义学院','通识','通识课',5,0,1,0),
(9302,'9302','大学生职业发展与就业指导',1001,1.5,'学生工作部','通识','通识课',4,0,1,0),
(9303,'9303','中国近现代史纲要',1001,3.0,'马克思主义学院','通识','思政课',6,0,1,0),
(9304,'9304','大学英语（四）',1001,2.0,'外国语学院','通识','公共课',5,0,1,0),
(9305,'9305','体育（四）',1001,1.0,'体育学院','通识','公共课',8,0,1,0),
(9401,'9401','开源软件项目实践',1001,2.0,'软件工程系','个性课程','创新实践',1,0,1,0),
(9402,'9402','企业级项目开发实训',1001,3.0,'软件工程系','个性课程','实践课',1,0,1,0),
(9403,'9403','科技创新训练',1001,2.0,'创新创业学院','个性课程','创新创业',2,0,1,0),
(9404,'9404','ACM 程序设计竞赛训练',1001,2.0,'软件工程系','个性课程','创新实践',1,0,1,0),
(9405,'9405','毕业设计（软件工程）',1001,8.0,'软件工程系','个性课程','毕业实践',6,0,1,0)
ON DUPLICATE KEY UPDATE id=id;

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
    INDEX idx_enrollment_course_status (course_id, status),
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

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
    INDEX idx_discussion_created_at (created_at),
    CONSTRAINT fk_discussion_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_discussion_author FOREIGN KEY (author_id) REFERENCES tb_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_discussion_parent FOREIGN KEY (parent_id) REFERENCES tb_discussion(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程讨论表';
