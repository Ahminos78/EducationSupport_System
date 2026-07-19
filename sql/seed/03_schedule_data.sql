USE edu_platform;

-- ============================================================
-- 排课种子数据
-- 每个教学班每周 2-3 次课，分散在周一至周五
-- 课时：上午 1-4 节（2课时段），下午 5-8 节，晚上 9-12 节
-- 教学周：1-16 周
-- class_id 使用 tb_course_class 实际 ID（course_id * 100 + 1）
-- ============================================================

DELETE FROM tb_course_schedule WHERE class_id IS NOT NULL;

-- ── 必修课程（teacher 101: t1）─────────────────────────────

-- class_id=910101 Java Web 开发实战 默认班: 每周3次 (course_id=9101)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910101, 1, 1, 2, 1, 16, 0, '教学楼A-301'),
(910101, 3, 3, 4, 1, 16, 0, '教学楼A-301'),
(910101, 5, 5, 6, 1, 16, 0, '实验楼B-205');

-- class_id=910201 数据库系统原理 默认班: 每周3次 (course_id=9102)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910201, 2, 1, 2, 1, 16, 0, '教学楼A-405'),
(910201, 4, 3, 4, 1, 16, 0, '教学楼A-405'),
(910201, 5, 3, 4, 1, 16, 0, '实验楼B-301');

-- class_id=910301 MyBatis Plus 企业开发 默认班: 每周2次 (course_id=9103)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910301, 1, 3, 4, 1, 16, 0, '教学楼A-302'),
(910301, 4, 1, 2, 1, 16, 0, '实验楼B-205');

-- class_id=910401 Spring Boot 微服务开发 默认班: 每周3次 (course_id=9104)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910401, 2, 3, 4, 1, 16, 0, '教学楼A-303'),
(910401, 4, 5, 6, 1, 16, 0, '教学楼A-303'),
(910401, 5, 1, 2, 1, 16, 0, '实验楼B-205');

-- class_id=910501 数据结构与算法 默认班: 每周3次 (course_id=9105)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910501, 1, 5, 6, 1, 16, 0, '教学楼C-101'),
(910501, 3, 1, 2, 1, 16, 0, '教学楼C-101'),
(910501, 5, 7, 8, 1, 16, 0, '教学楼C-101');

-- ── 必修课程（teacher 102: t2）─────────────────────────────

-- class_id=910601 操作系统 默认班: 每周3次 (course_id=9106)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910601, 1, 1, 2, 1, 16, 0, '教学楼C-201'),
(910601, 3, 5, 6, 1, 16, 0, '教学楼C-201'),
(910601, 4, 1, 2, 1, 16, 0, '教学楼C-201');

-- class_id=910701 计算机网络 默认班: 每周3次 (course_id=9107)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910701, 2, 1, 2, 1, 16, 0, '教学楼C-202'),
(910701, 4, 3, 4, 1, 16, 0, '教学楼C-202'),
(910701, 5, 5, 6, 1, 16, 0, '教学楼C-202');

-- class_id=910801 软件工程 默认班: 每周2次 (course_id=9108)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910801, 2, 5, 6, 1, 16, 0, '教学楼C-301'),
(910801, 4, 7, 8, 1, 16, 0, '教学楼C-301');

-- class_id=910901 编译原理 默认班: 每周2次 (course_id=9109)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(910901, 1, 7, 8, 1, 16, 0, '教学楼C-302'),
(910901, 3, 7, 8, 1, 16, 0, '教学楼C-302');

-- class_id=911001 Linux 系统管理 默认班: 每周2次（含实验）(course_id=9110)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(911001, 2, 7, 8, 1, 16, 0, '教学楼C-102'),
(911001, 4, 5, 6, 1, 16, 0, '实验楼B-301');

-- ── 选修课程（teacher 103: t3）─────────────────────────────

-- class_id=920101 Python 数据分析 默认班: 每周2次 (course_id=9201)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920101, 1, 5, 6, 1, 16, 0, '实验楼B-401'),
(920101, 3, 5, 6, 1, 16, 0, '实验楼B-401');

-- class_id=920201 Vue3 前端开发 默认班: 每周2次 (course_id=9202)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920201, 2, 3, 4, 1, 16, 0, '实验楼B-402'),
(920201, 5, 3, 4, 1, 16, 0, '实验楼B-402');

-- class_id=920301 人工智能基础 默认班: 每周2次 (course_id=9203)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920301, 1, 3, 4, 1, 16, 0, '教学楼D-101'),
(920301, 3, 1, 2, 1, 16, 0, '教学楼D-101');

-- ── 选修课程（teacher 104: t4）─────────────────────────────

-- class_id=920401 深度学习导论 默认班: 每周2次 (course_id=9204)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920401, 2, 5, 6, 1, 16, 0, '教学楼D-102'),
(920401, 5, 1, 2, 1, 16, 0, '教学楼D-102');

-- class_id=920501 云计算与容器技术 默认班: 每周2次 (course_id=9205)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920501, 3, 3, 4, 1, 16, 0, '实验楼B-301'),
(920501, 5, 7, 8, 1, 16, 0, '实验楼B-301');

-- class_id=920601 大数据技术基础 默认班: 每周2次 (course_id=9206)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(920601, 1, 1, 2, 1, 16, 0, '教学楼D-201'),
(920601, 4, 3, 4, 1, 16, 0, '教学楼D-201');

-- ── 通识课程（teacher 105: t5）─────────────────────────────

-- class_id=930101 大学生心理健康教育 默认班: 每周2次 (course_id=9301)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930101, 1, 7, 8, 1, 16, 0, '教学楼E-101'),
(930101, 3, 7, 8, 1, 16, 0, '教学楼E-101');

-- class_id=930201 大学生职业发展与就业指导 默认班: 每周1次 (course_id=9302)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930201, 5, 9, 10, 1, 16, 0, '教学楼E-201');

-- class_id=930301 中国近现代史纲要 默认班: 每周3次 (course_id=9303)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930301, 2, 1, 2, 1, 16, 0, '教学楼E-301'),
(930301, 3, 5, 6, 1, 16, 0, '教学楼E-301'),
(930301, 5, 5, 6, 1, 16, 0, '教学楼E-301');

-- class_id=930401 大学英语（四） 默认班: 每周2次 (course_id=9304)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930401, 1, 3, 4, 1, 16, 0, '教学楼E-401'),
(930401, 4, 1, 2, 1, 16, 0, '教学楼E-401');

-- class_id=930501 体育（四） 默认班: 每周2次 (course_id=9305)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(930501, 2, 9, 10, 1, 16, 0, '体育馆'),
(930501, 4, 9, 10, 1, 16, 0, '体育馆');

-- ── 个性课程（teacher 106: t6）─────────────────────────────

-- class_id=940101 开源软件项目实践 默认班: 每周2次 (course_id=9401)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940101, 2, 5, 6, 1, 16, 0, '实验楼B-501'),
(940101, 4, 5, 6, 1, 16, 0, '实验楼B-501');

-- class_id=940201 企业级项目开发实训 默认班: 每周3次 (course_id=9402)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940201, 1, 1, 2, 1, 16, 0, '实验楼B-502'),
(940201, 3, 3, 4, 1, 16, 0, '实验楼B-502'),
(940201, 5, 1, 2, 1, 16, 0, '实验楼B-502');

-- class_id=940301 科技创新训练 默认班: 每周2次 (course_id=9403)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940301, 1, 5, 6, 1, 16, 0, '教学楼D-301'),
(940301, 4, 7, 8, 1, 16, 0, '教学楼D-301');

-- class_id=940401 ACM 程序设计竞赛训练 默认班: 每周2次 (course_id=9404)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940401, 3, 9, 10, 1, 16, 0, '实验楼B-501'),
(940401, 5, 9, 10, 1, 16, 0, '实验楼B-501');

-- class_id=940501 毕业设计（软件工程） 默认班: 每周1次（集中指导）(course_id=9405)
INSERT INTO tb_course_schedule (class_id, day_of_week, start_period, end_period, start_week, end_week, week_type, location) VALUES
(940501, 4, 11, 12, 1, 16, 0, '学院楼-会议室');

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
