USE edu_platform;

-- ============================================================
-- 排课种子数据
-- 每个教学班每周 2-3 次课，分散在周一至周五
-- 课时：上午 1-4 节（2课时段），下午 5-8 节，晚上 9-12 节
-- 教学周：1-16 周
-- class_id 使用 tb_course_class 自增主键 (1-26)
-- ============================================================

DELETE FROM tb_course_schedule WHERE class_id IS NOT NULL;

-- ── 必修课程（teacher 101: t1）─────────────────────────────

-- class_id=1 Java Web 开发实战 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(1, 1, 1, 2, 1, 16, 0, '教学楼A-301'),
(1, 3, 3, 4, 1, 16, 0, '教学楼A-301'),
(1, 5, 5, 6, 1, 16, 0, '实验楼B-205');

-- class_id=2 数据库系统原理 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(2, 2, 1, 2, 1, 16, 0, '教学楼A-405'),
(2, 4, 3, 4, 1, 16, 0, '教学楼A-405'),
(2, 5, 3, 4, 1, 16, 0, '实验楼B-301');

-- class_id=3 MyBatis Plus 企业开发 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(3, 1, 3, 4, 1, 16, 0, '教学楼A-302'),
(3, 4, 1, 2, 1, 16, 0, '实验楼B-205');

-- class_id=4 Spring Boot 微服务开发 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(4, 2, 3, 4, 1, 16, 0, '教学楼A-303'),
(4, 4, 5, 6, 1, 16, 0, '教学楼A-303'),
(4, 5, 1, 2, 1, 16, 0, '实验楼B-205');

-- class_id=5 数据结构与算法 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(5, 1, 5, 6, 1, 16, 0, '教学楼C-101'),
(5, 3, 1, 2, 1, 16, 0, '教学楼C-101'),
(5, 5, 7, 8, 1, 16, 0, '教学楼C-101');

-- ── 必修课程（teacher 102: t2）─────────────────────────────

-- class_id=6 操作系统 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(6, 1, 1, 2, 1, 16, 0, '教学楼C-201'),
(6, 3, 5, 6, 1, 16, 0, '教学楼C-201'),
(6, 4, 1, 2, 1, 16, 0, '教学楼C-201');

-- class_id=7 计算机网络 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(7, 2, 1, 2, 1, 16, 0, '教学楼C-202'),
(7, 4, 3, 4, 1, 16, 0, '教学楼C-202'),
(7, 5, 5, 6, 1, 16, 0, '教学楼C-202');

-- class_id=8 软件工程 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(8, 2, 5, 6, 1, 16, 0, '教学楼C-301'),
(8, 4, 7, 8, 1, 16, 0, '教学楼C-301');

-- class_id=9 编译原理 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(9, 1, 7, 8, 1, 16, 0, '教学楼C-302'),
(9, 3, 7, 8, 1, 16, 0, '教学楼C-302');

-- class_id=10 Linux 系统管理 默认班: 每周2次（含实验）
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(10, 2, 7, 8, 1, 16, 0, '教学楼C-102'),
(10, 4, 5, 6, 1, 16, 0, '实验楼B-301');

-- ── 选修课程（teacher 103: t3）─────────────────────────────

-- class_id=11 Python 数据分析 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(11, 1, 5, 6, 1, 16, 0, '实验楼B-401'),
(11, 3, 5, 6, 1, 16, 0, '实验楼B-401');

-- class_id=12 Vue3 前端开发 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(12, 2, 3, 4, 1, 16, 0, '实验楼B-402'),
(12, 5, 3, 4, 1, 16, 0, '实验楼B-402');

-- class_id=13 人工智能基础 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(13, 1, 3, 4, 1, 16, 0, '教学楼D-101'),
(13, 3, 1, 2, 1, 16, 0, '教学楼D-101');

-- ── 选修课程（teacher 104: t4）─────────────────────────────

-- class_id=14 深度学习导论 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(14, 2, 5, 6, 1, 16, 0, '教学楼D-102'),
(14, 5, 1, 2, 1, 16, 0, '教学楼D-102');

-- class_id=15 云计算与容器技术 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(15, 3, 3, 4, 1, 16, 0, '实验楼B-301'),
(15, 5, 7, 8, 1, 16, 0, '实验楼B-301');

-- class_id=16 大数据技术基础 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(16, 1, 1, 2, 1, 16, 0, '教学楼D-201'),
(16, 4, 3, 4, 1, 16, 0, '教学楼D-201');

-- ── 通识课程（teacher 105: t5）─────────────────────────────

-- class_id=17 大学生心理健康教育 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(17, 1, 7, 8, 1, 16, 0, '教学楼E-101'),
(17, 3, 7, 8, 1, 16, 0, '教学楼E-101');

-- class_id=18 大学生职业发展与就业指导 默认班: 每周1次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(18, 5, 9, 10, 1, 16, 0, '教学楼E-201');

-- class_id=19 中国近现代史纲要 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(19, 2, 1, 2, 1, 16, 0, '教学楼E-301'),
(19, 3, 5, 6, 1, 16, 0, '教学楼E-301'),
(19, 5, 5, 6, 1, 16, 0, '教学楼E-301');

-- class_id=20 大学英语（四） 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(20, 1, 3, 4, 1, 16, 0, '教学楼E-401'),
(20, 4, 1, 2, 1, 16, 0, '教学楼E-401');

-- class_id=21 体育（四） 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(21, 2, 9, 10, 1, 16, 0, '体育馆'),
(21, 4, 9, 10, 1, 16, 0, '体育馆');

-- ── 个性课程（teacher 106: t6）─────────────────────────────

-- class_id=22 开源软件项目实践 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(22, 2, 5, 6, 1, 16, 0, '实验楼B-501'),
(22, 4, 5, 6, 1, 16, 0, '实验楼B-501');

-- class_id=23 企业级项目开发实训 默认班: 每周3次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(23, 1, 1, 2, 1, 16, 0, '实验楼B-502'),
(23, 3, 3, 4, 1, 16, 0, '实验楼B-502'),
(23, 5, 1, 2, 1, 16, 0, '实验楼B-502');

-- class_id=24 科技创新训练 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(24, 1, 5, 6, 1, 16, 0, '教学楼D-301'),
(24, 4, 7, 8, 1, 16, 0, '教学楼D-301');

-- class_id=25 ACM 程序设计竞赛训练 默认班: 每周2次
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(25, 3, 9, 10, 1, 16, 0, '实验楼B-501'),
(25, 5, 9, 10, 1, 16, 0, '实验楼B-501');

-- class_id=26 毕业设计（软件工程） 默认班: 每周1次（集中指导）
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(26, 4, 11, 12, 1, 16, 0, '学院楼-会议室');

-- ── 验证 ──────────────────────────────────────────────────
SELECT 
    s.class_id,
    c.name AS class_name,
    s.day_of_week,
    CONCAT('第', s.start_period, '-', s.end_period, '节') AS periods,
    CONCAT('第', s.start_week, '-', s.end_week, '周') AS weeks,
    s.location
FROM tb_course_schedule s
JOIN tb_course_class c ON c.id = s.class_id
ORDER BY s.day_of_week, s.start_period;
