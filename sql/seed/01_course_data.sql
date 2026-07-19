USE edu_platform;

-- ============================================================
-- 课程种子数据：课程描述补充 + 教师分配
-- 从以下文件合并：
--   backfill_course_descriptions.sql / fix-teacher-course.sql
--   seed_development_teachers_and_courses.sql
-- 脚本可重复执行，幂等安全。
-- ============================================================

-- ── 1. 课程描述补充 ──────────────────────────────────────────
-- 只补充 NULL 或空字符串简介，避免覆盖后续人工编辑的课程介绍。
UPDATE tb_course
SET description = CASE id
    WHEN 9101 THEN '面向软件工程实践，系统学习 Servlet、Spring Boot、RESTful API、权限认证与前后端联调，完成可部署的 Java Web 项目。'
    WHEN 9102 THEN '介绍关系数据库基本原理、数据建模、SQL、事务、索引与并发控制，通过实验掌握数据库设计、查询优化和日常管理方法。'
    WHEN 9103 THEN '围绕 MyBatis Plus 的实体映射、条件构造器、分页、逻辑删除与代码生成，掌握企业项目中的数据访问层开发与规范。'
    WHEN 9104 THEN '基于 Spring Boot 与 Spring Cloud 学习服务拆分、注册发现、配置管理、网关、容错和链路追踪，完成微服务项目实践。'
    WHEN 9105 THEN '学习线性表、树、图、查找、排序及常用算法设计方法，结合复杂度分析和编程练习提升问题建模与求解能力。'
    WHEN 9106 THEN '讲解进程与线程、处理器调度、内存管理、文件系统和设备管理，理解操作系统核心机制及并发程序运行原理。'
    WHEN 9107 THEN '系统学习计算机网络体系结构、TCP/IP 协议、路由交换、应用层协议与网络安全基础，并通过抓包实验分析通信过程。'
    WHEN 9108 THEN '介绍需求分析、软件设计、编码规范、测试、项目管理与持续交付，培养团队协作和完整软件生命周期实践能力。'
    WHEN 9109 THEN '学习词法分析、语法分析、语义分析、中间代码生成与优化，理解高级语言从源代码到目标程序的转换过程。'
    WHEN 9110 THEN '通过命令行、用户权限、进程服务、网络、存储和 Shell 脚本实践，掌握 Linux 服务器的基础配置与运维方法。'
    WHEN 9201 THEN '使用 Python、NumPy、Pandas 与可视化工具完成数据清洗、统计分析和结果展示，培养从原始数据提取信息的能力。'
    WHEN 9202 THEN '学习 Vue 3 组合式 API、组件化、路由、状态管理和工程化工具，完成响应式前端应用的设计、开发与联调。'
    WHEN 9203 THEN '介绍人工智能的基本概念、搜索、知识表示、机器学习与典型应用，通过案例建立 AI 问题建模和实现基础。'
    WHEN 9204 THEN '学习神经网络、反向传播、卷积网络、循环网络和模型训练方法，使用主流框架完成基础深度学习实验。'
    WHEN 9205 THEN '介绍云计算架构、虚拟化、Docker、容器编排与持续部署，通过实验掌握应用容器化和云原生部署基础。'
    WHEN 9206 THEN '学习分布式存储与计算、大数据处理流程及常用生态组件，理解海量数据采集、处理、分析的基本方法。'
    WHEN 9301 THEN '围绕自我认知、情绪调节、人际交往、压力管理和心理危机预防，帮助学生提升心理健康意识与自我调适能力。'
    WHEN 9302 THEN '通过职业认知、能力探索、简历面试和就业政策指导，帮助学生制定职业发展计划并提升求职实践能力。'
    WHEN 9303 THEN '梳理中国近现代社会发展的历史进程和重大事件，理解中国人民选择中国共产党、马克思主义和社会主义道路的历史逻辑。'
    WHEN 9304 THEN '训练英语听、说、读、写、译综合能力，结合学术与职场语境提升信息理解、表达和跨文化沟通水平。'
    WHEN 9305 THEN '通过专项运动训练提升力量、耐力、协调性与运动技能，掌握科学锻炼方法并形成健康生活习惯。'
    WHEN 9401 THEN '以真实开源仓库为载体，实践 Git 协作、Issue 管理、代码评审、测试和文档编写，完成一次规范的开源贡献。'
    WHEN 9402 THEN '按照企业研发流程开展需求分析、架构设计、协作开发、测试和部署，综合运用前后端技术完成项目交付。'
    WHEN 9403 THEN '面向实际问题开展选题调研、方案设计、原型实现和成果汇报，培养创新思维、科研方法与团队实践能力。'
    WHEN 9404 THEN '围绕算法与数据结构开展专题训练、模拟竞赛和代码复盘，提升程序设计速度、正确性与复杂问题求解能力。'
    WHEN 9405 THEN '在教师指导下完成软件工程课题的需求分析、系统设计、编码测试、论文撰写与答辩，形成完整毕业成果。'
    ELSE description
END
WHERE (description IS NULL OR TRIM(description) = '')
  AND id IN (
      9101, 9102, 9103, 9104, 9105, 9106, 9107, 9108, 9109, 9110,
      9201, 9202, 9203, 9204, 9205, 9206,
      9301, 9302, 9303, 9304, 9305,
      9401, 9402, 9403, 9404, 9405
  );

-- ── 2. 课程教师分配 ──────────────────────────────────────────
-- 将课程分配给快速登录教师 t1(101) ~ t6(106)
-- 必修课程 → t1(101), 选修课程按组分配
UPDATE tb_course SET teacher_id = 101 WHERE id BETWEEN 9101 AND 9105;
UPDATE tb_course SET teacher_id = 102 WHERE id BETWEEN 9106 AND 9110;
UPDATE tb_course SET teacher_id = 103 WHERE id IN (9201, 9202, 9203);
UPDATE tb_course SET teacher_id = 104 WHERE id IN (9204, 9205, 9206);
UPDATE tb_course SET teacher_id = 105 WHERE id BETWEEN 9301 AND 9305;
UPDATE tb_course SET teacher_id = 106 WHERE id BETWEEN 9401 AND 9405;

-- ── 3. 验证 ──────────────────────────────────────────────────
SELECT id, code, name, teacher_id, category,
       CASE WHEN description IS NULL OR TRIM(description) = '' THEN '⛔ 缺少描述' ELSE '✅' END AS desc_status
FROM tb_course
WHERE deleted = 0
ORDER BY id;

-- ── 4. 课程学年、学期、总学时 ──────────────────────────────────
UPDATE tb_course SET
    academic_year = '2025-2026',
    semester = 2,
    total_hours = CASE id
        WHEN 9101 THEN 48  WHEN 9102 THEN 56  WHEN 9103 THEN 32
        WHEN 9104 THEN 48  WHEN 9105 THEN 64  WHEN 9106 THEN 56
        WHEN 9107 THEN 56  WHEN 9108 THEN 48  WHEN 9109 THEN 48
        WHEN 9110 THEN 48
        WHEN 9201 THEN 32  WHEN 9202 THEN 32  WHEN 9203 THEN 40
        WHEN 9204 THEN 40  WHEN 9205 THEN 40  WHEN 9206 THEN 40
        WHEN 9301 THEN 32  WHEN 9302 THEN 24  WHEN 9303 THEN 48
        WHEN 9304 THEN 32  WHEN 9305 THEN 32
        WHEN 9401 THEN 48  WHEN 9402 THEN 64  WHEN 9403 THEN 48
        WHEN 9404 THEN 48  WHEN 9405 THEN 128
    END
WHERE id IN (9101,9102,9103,9104,9105,9106,9107,9108,9109,9110,
             9201,9202,9203,9204,9205,9206,
             9301,9302,9303,9304,9305,
             9401,9402,9403,9404,9405);

-- ── 5. 验证 ──────────────────────────────────────────────────
SELECT id, code, name, academic_year, semester, total_hours, credit
FROM tb_course
WHERE deleted = 0
ORDER BY id;

-- ── 6. 教学班种子数据 ──────────────────────────────────────
-- 每门课程创建一个默认教学班，class_id = course_id * 100 + 1
-- 后续可在管理端调整为实际分班
INSERT INTO tb_course_class (id, course_id, teacher_id, name, max_students, enrolled_count) VALUES
(910101, 9101, 101, 'Java Web 开发实战 默认班', 60, 0),
(910201, 9102, 101, '数据库系统原理 默认班', 60, 0),
(910301, 9103, 101, 'MyBatis Plus 企业开发 默认班', 60, 0),
(910401, 9104, 101, 'Spring Boot 微服务开发 默认班', 60, 0),
(910501, 9105, 101, '数据结构与算法 默认班', 60, 0),
(910601, 9106, 102, '操作系统 默认班', 60, 0),
(910701, 9107, 102, '计算机网络 默认班', 60, 0),
(910801, 9108, 102, '软件工程 默认班', 60, 0),
(910901, 9109, 102, '编译原理 默认班', 60, 0),
(911001, 9110, 102, 'Linux 系统管理 默认班', 60, 0),
(920101, 9201, 103, 'Python 数据分析 默认班', 60, 0),
(920201, 9202, 103, 'Vue3 前端开发 默认班', 60, 0),
(920301, 9203, 103, '人工智能基础 默认班', 60, 0),
(920401, 9204, 104, '深度学习导论 默认班', 60, 0),
(920501, 9205, 104, '云计算与容器技术 默认班', 60, 0),
(920601, 9206, 104, '大数据技术基础 默认班', 60, 0),
(930101, 9301, 105, '大学生心理健康教育 默认班', 60, 0),
(930201, 9302, 105, '大学生职业发展与就业指导 默认班', 60, 0),
(930301, 9303, 105, '中国近现代史纲要 默认班', 60, 0),
(930401, 9304, 105, '大学英语（四） 默认班', 60, 0),
(930501, 9305, 105, '体育（四） 默认班', 60, 0),
(940101, 9401, 106, '开源软件项目实践 默认班', 60, 0),
(940201, 9402, 106, '企业级项目开发实训 默认班', 60, 0),
(940301, 9403, 106, '科技创新训练 默认班', 60, 0),
(940401, 9404, 106, 'ACM 程序设计竞赛训练 默认班', 60, 0),
(940501, 9405, 106, '毕业设计（软件工程） 默认班', 60, 0)
ON DUPLICATE KEY UPDATE id=id;

-- ── 7. 成绩组成项种子数据（典型课程）──────────────────────
-- 课程 9101：Java Web（3学分），平时+期中+期末
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9101, '平时作业', 0.3000, 100, 1),
(9101, '期中考试', 0.3000, 100, 2),
(9101, '期末考试', 0.4000, 100, 3)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 课程 9105：数据结构（4学分），平时+实验+期中+期末
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9105, '平时作业', 0.2000, 100, 1),
(9105, '实验报告', 0.2000, 100, 2),
(9105, '期中考试', 0.2000, 100, 3),
(9105, '期末考试', 0.4000, 100, 4)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 课程 9201：Python 数据分析（2学分），作业+期末
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9201, '平时作业', 0.4000, 100, 1),
(9201, '期末考试', 0.6000, 100, 2)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 课程 9301：心理健康（2学分通识），平时+论文
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9301, '课堂参与', 0.3000, 100, 1),
(9301, '课程论文', 0.7000, 100, 2)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 课程 9405：毕业设计（8学分实践），中期+答辩
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9405, '中期检查', 0.3000, 100, 1),
(9405, '论文评阅', 0.2000, 100, 2),
(9405, '答辩成绩', 0.5000, 100, 3)
ON DUPLICATE KEY UPDATE name=VALUES(name);
