-- ============================================================
-- 成绩组成项种子数据（剩余 21 门课程）
-- 设计原则：
--   - 3学分以上专业课：平时作业 + 实验 + 期中 + 期末
--   - 2-3学分课程：平时作业 + 期末
--   - 通识课：课堂参与 + 课程论文/报告
--   - 实践课：项目/实验 + 答辩/报告
-- 幂等安全：ON DUPLICATE KEY 可重复执行
-- ============================================================

-- ── 9102 数据库系统原理 (3.5学分 必修 核心课) ──────────────
DELETE FROM tb_grade_component WHERE course_id = 9102;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9102, '平时作业',  0.2000, 100, 1),
(9102, '实验报告',  0.2000, 100, 2),
(9102, '期中考试',  0.2000, 100, 3),
(9102, '期末考试',  0.4000, 100, 4)
;

-- ── 9103 MyBatis Plus 企业开发 (2学分 必修 核心课) ──────────
DELETE FROM tb_grade_component WHERE course_id = 9103;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9103, '平时作业',  0.4000, 100, 1),
(9103, '上机考核',  0.2000, 100, 2),
(9103, '期末考试',  0.4000, 100, 3)
;

-- ── 9104 Spring Boot 微服务开发 (3学分 必修 核心课) ─────────
DELETE FROM tb_grade_component WHERE course_id = 9104;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9104, '平时作业',  0.2000, 100, 1),
(9104, '阶段项目',  0.3000, 100, 2),
(9104, '期末考试',  0.5000, 100, 3)
;

-- ── 9106 操作系统 (3.5学分 必修 核心课) ─────────────────────
DELETE FROM tb_grade_component WHERE course_id = 9106;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9106, '平时作业',  0.2000, 100, 1),
(9106, '实验报告',  0.2000, 100, 2),
(9106, '期中考试',  0.2000, 100, 3),
(9106, '期末考试',  0.4000, 100, 4)
;

-- ── 9107 计算机网络 (3.5学分 必修 核心课) ───────────────────
DELETE FROM tb_grade_component WHERE course_id = 9107;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9107, '平时作业',  0.2000, 100, 1),
(9107, '实验报告',  0.2000, 100, 2),
(9107, '期中考试',  0.2000, 100, 3),
(9107, '期末考试',  0.4000, 100, 4)
;

-- ── 9108 软件工程 (3学分 必修 核心课) ───────────────────────
DELETE FROM tb_grade_component WHERE course_id = 9108;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9108, '平时作业',  0.2000, 100, 1),
(9108, '团队项目',  0.3000, 100, 2),
(9108, '期末考试',  0.5000, 100, 3)
;

-- ── 9109 编译原理 (3学分 必修 专业课) ───────────────────────
DELETE FROM tb_grade_component WHERE course_id = 9109;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9109, '平时作业',  0.2000, 100, 1),
(9109, '实验报告',  0.3000, 100, 2),
(9109, '期末考试',  0.5000, 100, 3)
;

-- ── 9110 Linux 系统管理 (2.5学分 必修 实践课) ───────────────
DELETE FROM tb_grade_component WHERE course_id = 9110;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9110, '平时作业',  0.2000, 100, 1),
(9110, '上机操作',  0.3000, 100, 2),
(9110, '期末考核',  0.5000, 100, 3)
;

-- ── 9202 Vue3 前端开发 (2学分 选修 专业选修) ────────────────
DELETE FROM tb_grade_component WHERE course_id = 9202;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9202, '平时作业',  0.3000, 100, 1),
(9202, '项目实战',  0.3000, 100, 2),
(9202, '期末考试',  0.4000, 100, 3)
;

-- ── 9203 人工智能基础 (2.5学分 选修 AI课程) ─────────────────
DELETE FROM tb_grade_component WHERE course_id = 9203;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9203, '平时作业',  0.3000, 100, 1),
(9203, '实验报告',  0.3000, 100, 2),
(9203, '期末考试',  0.4000, 100, 3)
;

-- ── 9204 深度学习导论 (2.5学分 选修 AI课程) ─────────────────
DELETE FROM tb_grade_component WHERE course_id = 9204;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9204, '平时作业',  0.2000, 100, 1),
(9204, '实验项目',  0.3000, 100, 2),
(9204, '期末项目',  0.5000, 100, 3)
;

-- ── 9205 云计算与容器技术 (2学分 选修 专业选修) ────────────
DELETE FROM tb_grade_component WHERE course_id = 9205;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9205, '平时作业',  0.3000, 100, 1),
(9205, '实验操作',  0.3000, 100, 2),
(9205, '期末考试',  0.4000, 100, 3)
;

-- ── 9206 大数据技术基础 (2.5学分 选修 专业选修) ────────────
DELETE FROM tb_grade_component WHERE course_id = 9206;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9206, '平时作业',  0.2000, 100, 1),
(9206, '实验报告',  0.3000, 100, 2),
(9206, '期末考试',  0.5000, 100, 3)
;

-- ── 9302 大学生职业发展与就业指导 (1.5学分 通识) ──────────
DELETE FROM tb_grade_component WHERE course_id = 9302;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9302, '课堂参与',  0.3000, 100, 1),
(9302, '职业生涯规划书',  0.7000, 100, 2)
;

-- ── 9303 中国近现代史纲要 (3学分 通识 思政课) ──────────────
DELETE FROM tb_grade_component WHERE course_id = 9303;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9303, '平时作业',  0.2000, 100, 1),
(9303, '课堂表现',  0.1000, 100, 2),
(9303, '期中考试',  0.2000, 100, 3),
(9303, '期末考试',  0.5000, 100, 4)
;

-- ── 9304 大学英语（四）(2学分 通识 公共课) ──────────────────
DELETE FROM tb_grade_component WHERE course_id = 9304;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9304, '平时作业',  0.2000, 100, 1),
(9304, '课堂参与',  0.1000, 100, 2),
(9304, '期中考试',  0.2000, 100, 3),
(9304, '期末考试',  0.5000, 100, 4)
;

-- ── 9305 体育（四）(1学分 通识 公共课) ─────────────────────
DELETE FROM tb_grade_component WHERE course_id = 9305;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9305, '考勤表现',  0.2000, 100, 1),
(9305, '体能测试',  0.3000, 100, 2),
(9305, '技能考核',  0.5000, 100, 3)
;

-- ── 9401 开源软件项目实践 (2学分 个性 创新实践) ────────────
DELETE FROM tb_grade_component WHERE course_id = 9401;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9401, '项目贡献',  0.4000, 100, 1),
(9401, '项目报告',  0.3000, 100, 2),
(9401, '答辩演示',  0.3000, 100, 3)
;

-- ── 9402 企业级项目开发实训 (3学分 个性 实践课) ────────────
DELETE FROM tb_grade_component WHERE course_id = 9402;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9402, '需求分析',  0.1500, 100, 1),
(9402, '系统设计',  0.1500, 100, 2),
(9402, '项目实现',  0.4000, 100, 3),
(9402, '答辩演示',  0.3000, 100, 4)
;

-- ── 9403 科技创新训练 (2学分 个性 创新创业) ────────────────
DELETE FROM tb_grade_component WHERE course_id = 9403;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9403, '创新报告',  0.3000, 100, 1),
(9403, '成果展示',  0.3000, 100, 2),
(9403, '答辩考核',  0.4000, 100, 3)
;

-- ── 9404 ACM 程序设计竞赛训练 (2学分 个性 创新实践) ────────
DELETE FROM tb_grade_component WHERE course_id = 9404;
INSERT INTO tb_grade_component (course_id, name, weight, max_score, sort_order) VALUES
(9404, '日常训练',  0.3000, 100, 1),
(9404, '模拟竞赛',  0.3000, 100, 2),
(9404, '竞赛成绩',  0.4000, 100, 3)
;

-- ── 验证：查看所有成绩组成项 ─────────────────────────────────
SELECT c.id, c.name AS course_name, c.credit, g.name AS component,
       g.weight, g.max_score
FROM tb_course c
JOIN tb_grade_component g ON g.course_id = c.id
WHERE c.deleted = 0
ORDER BY c.id, g.sort_order;
