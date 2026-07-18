USE edu_platform;

-- ============================================================
-- 为每个课程添加第 2、3 个教学班（不同教师或同教师多班）
-- 然后为新教学班创建排课数据
-- ============================================================

START TRANSACTION;

-- ── 1. 添加教学班 ──────────────────────────────────────────
-- 格式：原教师 + 交叉分配，每门课 2-3 个班

-- 必修课程（teacher 101: t1 / 102: t2 交叉）
INSERT INTO tb_course_class (course_id, teacher_id, name, max_students, enrolled_count) VALUES
-- 9101 Java Web: t1默认 + t2二班
(9101, 102, 'Java Web 开发实战 二班', 50, 0),
-- 9102 数据库: t1默认 + t2二班
(9102, 102, '数据库系统原理 二班', 50, 0),
-- 9103 MyBatis Plus: t1默认 + t2二班
(9103, 102, 'MyBatis Plus 企业开发 二班', 50, 0),
-- 9104 Spring Boot: t1默认 + t2二班
(9104, 102, 'Spring Boot 微服务开发 二班', 50, 0),
-- 9105 数据结构: t1默认 + t2二班
(9105, 102, '数据结构与算法 二班', 50, 0),
-- 9106 操作系统: t2默认 + t1二班
(9106, 101, '操作系统 二班', 50, 0),
-- 9107 计算机网络: t2默认 + t1二班
(9107, 101, '计算机网络 二班', 50, 0),
-- 9108 软件工程: t2默认 + t1三班
(9108, 101, '软件工程 二班', 50, 0),
-- 9109 编译原理: t2默认 + t1三班
(9109, 101, '编译原理 二班', 50, 0),
-- 9110 Linux: t2默认 + t1三班
(9110, 101, 'Linux 系统管理 二班', 50, 0),

-- 选修课程（teacher 103: t3 / 104: t4 交叉）
-- 9201 Python: t3默认 + t4二班
(9201, 104, 'Python 数据分析 二班', 40, 0),
-- 9202 Vue3: t3默认 + t4二班
(9202, 104, 'Vue3 前端开发 二班', 40, 0),
-- 9203 人工智能基础: t3默认 + t4二班
(9203, 104, '人工智能基础 二班', 40, 0),
-- 9204 深度学习: t4默认 + t3二班
(9204, 103, '深度学习导论 二班', 40, 0),
-- 9205 云计算: t4默认 + t3二班
(9205, 103, '云计算与容器技术 二班', 40, 0),
-- 9206 大数据: t4默认 + t3二班
(9206, 103, '大数据技术基础 二班', 40, 0),

-- 通识课程（teacher 105: t5 默认，部分加二班）
-- 9301 心理健康: t5默认 + t6二班
(9301, 106, '大学生心理健康教育 二班', 80, 0),
-- 9303 近现代史: t5默认 + t6二班
(9303, 106, '中国近现代史纲要 二班', 80, 0),

-- 个性课程（teacher 106: t6 默认，部分加二班）
-- 9401 开源实践: t6默认 + t5二班
(9401, 105, '开源软件项目实践 二班', 40, 0),
-- 9402 企业实训: t6默认 + t5二班
(9402, 105, '企业级项目开发实训 二班', 40, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ── 2. 获取新教学班 ID 并创建排课 ──────────────────────────
-- 新班 ID 从 27 开始递增

-- 27: Java Web 二班 (t2) — 周二/四/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(27, 2, 1, 2, 1, 16, 0, '教学楼A-302'),
(27, 4, 1, 2, 1, 16, 0, '教学楼A-302'),
(27, 6, 3, 4, 1, 16, 0, '实验楼B-206');

-- 28: 数据库 二班 (t2) — 周一/三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(28, 1, 3, 4, 1, 16, 0, '教学楼A-406'),
(28, 3, 1, 2, 1, 16, 0, '教学楼A-406'),
(28, 5, 1, 2, 1, 16, 0, '实验楼B-302');

-- 29: MyBatis Plus 二班 (t2) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(29, 3, 3, 4, 1, 16, 0, '教学楼A-303'),
(29, 5, 5, 6, 1, 16, 0, '实验楼B-206');

-- 30: Spring Boot 二班 (t2) — 周一/三/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(30, 1, 5, 6, 1, 16, 0, '教学楼A-304'),
(30, 3, 5, 6, 1, 16, 0, '教学楼A-304'),
(30, 6, 1, 2, 1, 16, 0, '实验楼B-206');

-- 31: 数据结构 二班 (t2) — 周二/四/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(31, 2, 3, 4, 1, 16, 0, '教学楼C-102'),
(31, 4, 3, 4, 1, 16, 0, '教学楼C-102'),
(31, 6, 5, 6, 1, 16, 0, '教学楼C-102');

-- 32: 操作系统 二班 (t1) — 周二/四/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(32, 2, 3, 4, 1, 16, 0, '教学楼C-203'),
(32, 4, 5, 6, 1, 16, 0, '教学楼C-203'),
(32, 5, 3, 4, 1, 16, 0, '教学楼C-203');

-- 33: 计算机网络 二班 (t1) — 周一/三/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(33, 1, 1, 2, 1, 16, 0, '教学楼C-204'),
(33, 3, 3, 4, 1, 16, 0, '教学楼C-204'),
(33, 4, 1, 2, 1, 16, 0, '教学楼C-204');

-- 34: 软件工程 二班 (t1) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(34, 1, 7, 8, 1, 16, 0, '教学楼C-303'),
(34, 4, 5, 6, 1, 16, 0, '教学楼C-303');

-- 35: 编译原理 二班 (t1) — 周二/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(35, 2, 1, 2, 1, 16, 0, '教学楼C-304'),
(35, 5, 7, 8, 1, 16, 0, '教学楼C-304');

-- 36: Linux 二班 (t1) — 周一/三
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(36, 1, 3, 4, 1, 16, 0, '教学楼C-103'),
(36, 3, 7, 8, 1, 16, 0, '实验楼B-302');

-- 37: Python 二班 (t4) — 周二/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(37, 2, 7, 8, 1, 16, 0, '实验楼B-403'),
(37, 4, 5, 6, 1, 16, 0, '实验楼B-403');

-- 38: Vue3 二班 (t4) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(38, 3, 3, 4, 1, 16, 0, '实验楼B-404'),
(38, 5, 1, 2, 1, 16, 0, '实验楼B-404');

-- 39: 人工智能基础 二班 (t4) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(39, 1, 7, 8, 1, 16, 0, '教学楼D-103'),
(39, 4, 3, 4, 1, 16, 0, '教学楼D-103');

-- 40: 深度学习 二班 (t3) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(40, 3, 1, 2, 1, 16, 0, '教学楼D-104'),
(40, 5, 3, 4, 1, 16, 0, '教学楼D-104');

-- 41: 云计算 二班 (t3) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(41, 1, 1, 2, 1, 16, 0, '实验楼B-302'),
(41, 4, 7, 8, 1, 16, 0, '实验楼B-302');

-- 42: 大数据 二班 (t3) — 周二/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(42, 2, 5, 6, 1, 16, 0, '教学楼D-203'),
(42, 5, 5, 6, 1, 16, 0, '教学楼D-203');

-- 43: 心理健康 二班 (t6) — 周二/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(43, 2, 7, 8, 1, 16, 0, '教学楼E-103'),
(43, 4, 7, 8, 1, 16, 0, '教学楼E-103');

-- 44: 近现代史 二班 (t6) — 周一/三/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(44, 1, 3, 4, 1, 16, 0, '教学楼E-303'),
(44, 3, 3, 4, 1, 16, 0, '教学楼E-303'),
(44, 5, 1, 2, 1, 16, 0, '教学楼E-303');

-- 45: 开源实践 二班 (t5) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(45, 3, 5, 6, 1, 16, 0, '实验楼B-503'),
(45, 5, 5, 6, 1, 16, 0, '实验楼B-503');

-- 46: 企业实训 二班 (t5) — 周二/四/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(46, 2, 1, 2, 1, 16, 0, '实验楼B-504'),
(46, 4, 1, 2, 1, 16, 0, '实验楼B-504'),
(46, 5, 3, 4, 1, 16, 0, '实验楼B-504');

COMMIT;

-- ── 3. 验证 ──────────────────────────────────────────────────
SELECT cc.id, cc.course_id, c.name AS course_name, cc.name AS class_name,
       coalesce(nullif(u.nickname, ''), u.username) AS teacher_name,
       cc.max_students, cc.enrolled_count
FROM tb_course_class cc
JOIN tb_course c ON c.id = cc.course_id
LEFT JOIN tb_user u ON u.id = cc.teacher_id
ORDER BY cc.course_id, cc.id;

SELECT class_id, COUNT(*) AS schedule_count
FROM tb_course_schedule
GROUP BY class_id
ORDER BY class_id;
