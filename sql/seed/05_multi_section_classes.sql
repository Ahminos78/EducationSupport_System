USE edu_platform;

-- ============================================================
-- 为每个课程添加第 2 个教学班（不同教师或同教师多班）
-- 然后为新教学班创建排课数据
-- class_id 使用 course_id * 100 + 2 规则（二班）
-- ============================================================

START TRANSACTION;

-- ── 1. 添加教学班 ──────────────────────────────────────────
-- 格式：原教师 + 交叉分配，每门课 2 个班
-- class_id = course_id * 100 + 2

-- 必修课程（teacher 101: t1 / 102: t2 交叉）
INSERT INTO tb_course_class (id, course_id, teacher_id, name, max_students, enrolled_count) VALUES
-- 9101 Java Web: t1默认 + t2二班
(910102, 9101, 102, 'Java Web 开发实战 二班', 50, 0),
-- 9102 数据库: t1默认 + t2二班
(910202, 9102, 102, '数据库系统原理 二班', 50, 0),
-- 9103 MyBatis Plus: t1默认 + t2二班
(910302, 9103, 102, 'MyBatis Plus 企业开发 二班', 50, 0),
-- 9104 Spring Boot: t1默认 + t2二班
(910402, 9104, 102, 'Spring Boot 微服务开发 二班', 50, 0),
-- 9105 数据结构: t1默认 + t2二班
(910502, 9105, 102, '数据结构与算法 二班', 50, 0),
-- 9106 操作系统: t2默认 + t1二班
(910602, 9106, 101, '操作系统 二班', 50, 0),
-- 9107 计算机网络: t2默认 + t1二班
(910702, 9107, 101, '计算机网络 二班', 50, 0),
-- 9108 软件工程: t2默认 + t1三班
(910802, 9108, 101, '软件工程 二班', 50, 0),
-- 9109 编译原理: t2默认 + t1三班
(910902, 9109, 101, '编译原理 二班', 50, 0),
-- 9110 Linux: t2默认 + t1三班
(911002, 9110, 101, 'Linux 系统管理 二班', 50, 0),

-- 选修课程（teacher 103: t3 / 104: t4 交叉）
-- 9201 Python: t3默认 + t4二班
(920102, 9201, 104, 'Python 数据分析 二班', 40, 0),
-- 9202 Vue3: t3默认 + t4二班
(920202, 9202, 104, 'Vue3 前端开发 二班', 40, 0),
-- 9203 人工智能基础: t3默认 + t4二班
(920302, 9203, 104, '人工智能基础 二班', 40, 0),
-- 9204 深度学习: t4默认 + t3二班
(920402, 9204, 103, '深度学习导论 二班', 40, 0),
-- 9205 云计算: t4默认 + t3二班
(920502, 9205, 103, '云计算与容器技术 二班', 40, 0),
-- 9206 大数据: t4默认 + t3二班
(920602, 9206, 103, '大数据技术基础 二班', 40, 0),

-- 通识课程（teacher 105: t5 默认，部分加二班）
-- 9301 心理健康: t5默认 + t6二班
(930102, 9301, 106, '大学生心理健康教育 二班', 80, 0),
-- 9303 近现代史: t5默认 + t6二班
(930302, 9303, 106, '中国近现代史纲要 二班', 80, 0),

-- 个性课程（teacher 106: t6 默认，部分加二班）
-- 9401 开源实践: t6默认 + t5二班
(940102, 9401, 105, '开源软件项目实践 二班', 40, 0),
-- 9402 企业实训: t6默认 + t5二班
(940202, 9402, 105, '企业级项目开发实训 二班', 40, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), teacher_id=VALUES(teacher_id);

-- ── 2. 为新教学班创建排课 ──────────────────────────────

-- 910102: Java Web 二班 (t2) — 周二/四/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910102, 2, 1, 2, 1, 16, 0, '教学楼A-302'),
(910102, 4, 1, 2, 1, 16, 0, '教学楼A-302'),
(910102, 6, 3, 4, 1, 16, 0, '实验楼B-206');

-- 910202: 数据库 二班 (t2) — 周一/三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910202, 1, 3, 4, 1, 16, 0, '教学楼A-406'),
(910202, 3, 1, 2, 1, 16, 0, '教学楼A-406'),
(910202, 5, 1, 2, 1, 16, 0, '实验楼B-302');

-- 910302: MyBatis Plus 二班 (t2) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910302, 3, 3, 4, 1, 16, 0, '教学楼A-303'),
(910302, 5, 5, 6, 1, 16, 0, '实验楼B-206');

-- 910402: Spring Boot 二班 (t2) — 周一/三/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910402, 1, 5, 6, 1, 16, 0, '教学楼A-304'),
(910402, 3, 5, 6, 1, 16, 0, '教学楼A-304'),
(910402, 6, 1, 2, 1, 16, 0, '实验楼B-206');

-- 910502: 数据结构 二班 (t2) — 周二/四/六
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910502, 2, 3, 4, 1, 16, 0, '教学楼C-102'),
(910502, 4, 3, 4, 1, 16, 0, '教学楼C-102'),
(910502, 6, 5, 6, 1, 16, 0, '教学楼C-102');

-- 910602: 操作系统 二班 (t1) — 周二/四/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910602, 2, 3, 4, 1, 16, 0, '教学楼C-203'),
(910602, 4, 5, 6, 1, 16, 0, '教学楼C-203'),
(910602, 5, 3, 4, 1, 16, 0, '教学楼C-203');

-- 910702: 计算机网络 二班 (t1) — 周一/三/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910702, 1, 1, 2, 1, 16, 0, '教学楼C-204'),
(910702, 3, 3, 4, 1, 16, 0, '教学楼C-204'),
(910702, 4, 1, 2, 1, 16, 0, '教学楼C-204');

-- 910802: 软件工程 二班 (t1) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910802, 1, 7, 8, 1, 16, 0, '教学楼C-303'),
(910802, 4, 5, 6, 1, 16, 0, '教学楼C-303');

-- 910902: 编译原理 二班 (t1) — 周二/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910902, 2, 1, 2, 1, 16, 0, '教学楼C-304'),
(910902, 5, 7, 8, 1, 16, 0, '教学楼C-304');

-- 911002: Linux 二班 (t1) — 周一/三
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(911002, 1, 3, 4, 1, 16, 0, '教学楼C-103'),
(911002, 3, 7, 8, 1, 16, 0, '实验楼B-302');

-- 920102: Python 二班 (t4) — 周二/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920102, 2, 7, 8, 1, 16, 0, '实验楼B-403'),
(920102, 4, 5, 6, 1, 16, 0, '实验楼B-403');

-- 920202: Vue3 二班 (t4) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920202, 3, 3, 4, 1, 16, 0, '实验楼B-404'),
(920202, 5, 1, 2, 1, 16, 0, '实验楼B-404');

-- 920302: 人工智能基础 二班 (t4) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920302, 1, 7, 8, 1, 16, 0, '教学楼D-103'),
(920302, 4, 3, 4, 1, 16, 0, '教学楼D-103');

-- 920402: 深度学习 二班 (t3) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920402, 3, 1, 2, 1, 16, 0, '教学楼D-104'),
(920402, 5, 3, 4, 1, 16, 0, '教学楼D-104');

-- 920502: 云计算 二班 (t3) — 周一/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920502, 1, 1, 2, 1, 16, 0, '实验楼B-302'),
(920502, 4, 7, 8, 1, 16, 0, '实验楼B-302');

-- 920602: 大数据 二班 (t3) — 周二/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920602, 2, 5, 6, 1, 16, 0, '教学楼D-203'),
(920602, 5, 5, 6, 1, 16, 0, '教学楼D-203');

-- 930102: 心理健康 二班 (t6) — 周二/四
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930102, 2, 7, 8, 1, 16, 0, '教学楼E-103'),
(930102, 4, 7, 8, 1, 16, 0, '教学楼E-103');

-- 930302: 近现代史 二班 (t6) — 周一/三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930302, 1, 3, 4, 1, 16, 0, '教学楼E-303'),
(930302, 3, 3, 4, 1, 16, 0, '教学楼E-303'),
(930302, 5, 1, 2, 1, 16, 0, '教学楼E-303');

-- 940102: 开源实践 二班 (t5) — 周三/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940102, 3, 5, 6, 1, 16, 0, '实验楼B-503'),
(940102, 5, 5, 6, 1, 16, 0, '实验楼B-503');

-- 940202: 企业实训 二班 (t5) — 周二/四/五
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940202, 2, 1, 2, 1, 16, 0, '实验楼B-504'),
(940202, 4, 1, 2, 1, 16, 0, '实验楼B-504'),
(940202, 5, 3, 4, 1, 16, 0, '实验楼B-504');

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
