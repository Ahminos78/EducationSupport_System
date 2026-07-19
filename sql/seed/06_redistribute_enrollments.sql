USE edu_platform;

-- ============================================================
-- 将学生分配到不同教学班（一班 + 二班混合）
-- class_id 使用数据库实际 ID 值
-- 需要先执行 05_multi_section_classes.sql 创建二班
-- ============================================================

START TRANSACTION;

-- 清理旧数据（status=2 已退选和 status=4 审核不通过的记录）
DELETE FROM tb_enrollment WHERE status IN (2, 4);

-- 学生 201：后端与数据分析方向 → 分配到不同班（部分一班，部分二班）
UPDATE tb_enrollment SET class_id = 910101 WHERE student_id = 201 AND course_id = 9101 AND status = 1;
UPDATE tb_enrollment SET class_id = 910202 WHERE student_id = 201 AND course_id = 9102 AND status = 1;
UPDATE tb_enrollment SET class_id = 910501 WHERE student_id = 201 AND course_id = 9105 AND status = 1;
UPDATE tb_enrollment SET class_id = 920101 WHERE student_id = 201 AND course_id = 9201 AND status = 1;
UPDATE tb_enrollment SET class_id = 930101 WHERE student_id = 201 AND course_id = 9301 AND status = 1;
UPDATE tb_enrollment SET class_id = 940101 WHERE student_id = 201 AND course_id = 9401 AND status = 1;

-- 学生 202：全栈开发方向
UPDATE tb_enrollment SET class_id = 910102 WHERE student_id = 202 AND course_id = 9101 AND status = 1;
UPDATE tb_enrollment SET class_id = 910301 WHERE student_id = 202 AND course_id = 9103 AND status = 1;
UPDATE tb_enrollment SET class_id = 910401 WHERE student_id = 202 AND course_id = 9104 AND status = 1;
UPDATE tb_enrollment SET class_id = 920202 WHERE student_id = 202 AND course_id = 9202 AND status = 1;
UPDATE tb_enrollment SET class_id = 930401 WHERE student_id = 202 AND course_id = 9304 AND status = 1;
UPDATE tb_enrollment SET class_id = 940202 WHERE student_id = 202 AND course_id = 9402 AND status = 1;

-- 学生 203：人工智能方向
UPDATE tb_enrollment SET class_id = 910202 WHERE student_id = 203 AND course_id = 9102 AND status = 1;
UPDATE tb_enrollment SET class_id = 910302 WHERE student_id = 203 AND course_id = 9103 AND status = 1;
UPDATE tb_enrollment SET class_id = 910502 WHERE student_id = 203 AND course_id = 9105 AND status = 1;
UPDATE tb_enrollment SET class_id = 920301 WHERE student_id = 203 AND course_id = 9203 AND status = 1;
UPDATE tb_enrollment SET class_id = 920401 WHERE student_id = 203 AND course_id = 9204 AND status = 1;
UPDATE tb_enrollment SET class_id = 940301 WHERE student_id = 203 AND course_id = 9403 AND status = 1;

-- 学生 204：云计算与系统方向
UPDATE tb_enrollment SET class_id = 910402 WHERE student_id = 204 AND course_id = 9104 AND status = 1;
UPDATE tb_enrollment SET class_id = 910602 WHERE student_id = 204 AND course_id = 9106 AND status = 1;
UPDATE tb_enrollment SET class_id = 910702 WHERE student_id = 204 AND course_id = 9107 AND status = 1;
UPDATE tb_enrollment SET class_id = 920502 WHERE student_id = 204 AND course_id = 9205 AND status = 1;
UPDATE tb_enrollment SET class_id = 930201 WHERE student_id = 204 AND course_id = 9302 AND status = 1;
UPDATE tb_enrollment SET class_id = 940201 WHERE student_id = 204 AND course_id = 9402 AND status = 1;

-- 学生 205：算法与工程实践方向
UPDATE tb_enrollment SET class_id = 910501 WHERE student_id = 205 AND course_id = 9105 AND status = 1;
UPDATE tb_enrollment SET class_id = 910802 WHERE student_id = 205 AND course_id = 9108 AND status = 1;
UPDATE tb_enrollment SET class_id = 911002 WHERE student_id = 205 AND course_id = 9110 AND status = 1;
UPDATE tb_enrollment SET class_id = 920602 WHERE student_id = 205 AND course_id = 9206 AND status = 1;
UPDATE tb_enrollment SET class_id = 930501 WHERE student_id = 205 AND course_id = 9305 AND status = 1;
UPDATE tb_enrollment SET class_id = 940401 WHERE student_id = 205 AND course_id = 9404 AND status = 1;

-- 学生 206
UPDATE tb_enrollment SET class_id = 910101 WHERE student_id = 206 AND course_id = 9102 AND status = 1;
UPDATE tb_enrollment SET class_id = 930201 WHERE student_id = 206 AND course_id = 9302 AND status = 1;

-- 学生 207
UPDATE tb_enrollment SET class_id = 910302 WHERE student_id = 207 AND course_id = 9103 AND status = 1;
UPDATE tb_enrollment SET class_id = 920602 WHERE student_id = 207 AND course_id = 9206 AND status = 1;
UPDATE tb_enrollment SET class_id = 940202 WHERE student_id = 207 AND course_id = 9402 AND status = 1;

-- 学生 208
UPDATE tb_enrollment SET class_id = 910402 WHERE student_id = 208 AND course_id = 9104 AND status = 1;
UPDATE tb_enrollment SET class_id = 930302 WHERE student_id = 208 AND course_id = 9303 AND status = 1;

-- 学生 209
UPDATE tb_enrollment SET class_id = 920101 WHERE student_id = 209 AND course_id = 9201 AND status = 1;
UPDATE tb_enrollment SET class_id = 940301 WHERE student_id = 209 AND course_id = 9403 AND status = 1;

-- 学生 210
UPDATE tb_enrollment SET class_id = 910501 WHERE student_id = 210 AND course_id = 9105 AND status = 1;
UPDATE tb_enrollment SET class_id = 930401 WHERE student_id = 210 AND course_id = 9304 AND status = 1;

-- 学生 211
UPDATE tb_enrollment SET class_id = 910602 WHERE student_id = 211 AND course_id = 9106 AND status = 1;
UPDATE tb_enrollment SET class_id = 920102 WHERE student_id = 211 AND course_id = 9201 AND status = 1;

-- 学生 212
UPDATE tb_enrollment SET class_id = 910702 WHERE student_id = 212 AND course_id = 9107 AND status = 1;
UPDATE tb_enrollment SET class_id = 920302 WHERE student_id = 212 AND course_id = 9203 AND status = 1;

-- 学生 213
UPDATE tb_enrollment SET class_id = 910802 WHERE student_id = 213 AND course_id = 9108 AND status = 1;
UPDATE tb_enrollment SET class_id = 920402 WHERE student_id = 213 AND course_id = 9204 AND status = 1;

-- 学生 214
UPDATE tb_enrollment SET class_id = 910902 WHERE student_id = 214 AND course_id = 9109 AND status = 1;
UPDATE tb_enrollment SET class_id = 930102 WHERE student_id = 214 AND course_id = 9301 AND status = 1;

-- 学生 215
UPDATE tb_enrollment SET class_id = 911002 WHERE student_id = 215 AND course_id = 9110 AND status = 1;
UPDATE tb_enrollment SET class_id = 940102 WHERE student_id = 215 AND course_id = 9401 AND status = 1;

-- ── 同步 enrolled_count ──────────────────────────────────────
UPDATE tb_course_class cc
SET cc.enrolled_count = (
    SELECT COUNT(*)
    FROM tb_enrollment e
    WHERE e.class_id = cc.id
      AND e.status = 1
);

-- ── 同步 tb_course.enrolled_count ──────────────────────────
UPDATE tb_course c
SET c.enrolled_count = (
    SELECT COUNT(*)
    FROM tb_enrollment e
    WHERE e.course_id = c.id
      AND e.status = 1
)
WHERE c.deleted = 0;

COMMIT;

-- ── 验证 ──────────────────────────────────────────────────
SELECT student_id, COUNT(*) AS approved_courses
FROM tb_enrollment
WHERE status = 1
GROUP BY student_id
ORDER BY student_id;

SELECT cc.id, cc.name, cc.enrolled_count,
       (SELECT COUNT(*) FROM tb_enrollment e WHERE e.class_id = cc.id AND e.status = 1) AS actual
FROM tb_course_class cc
WHERE cc.enrolled_count > 0
ORDER BY cc.id;
