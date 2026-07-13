USE edu_platform;

-- 为 student_liu 准备“我的课程”测试数据：3 门必修、3 门其他类型课程。
START TRANSACTION;

INSERT INTO tb_enrollment (
    course_id,
    student_id,
    status,
    apply_reason,
    review_comment,
    applied_at,
    reviewed_at
)
SELECT
    selected.course_id,
    student.id,
    1,
    '首页我的课程功能测试',
    '测试数据：审核通过',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT 9101 AS course_id
    UNION ALL SELECT 9102
    UNION ALL SELECT 9105
    UNION ALL SELECT 9201
    UNION ALL SELECT 9301
    UNION ALL SELECT 9401
) selected
JOIN tb_course course ON course.id = selected.course_id AND course.deleted = 0
JOIN tb_user student
    ON student.username = 'student_liu'
    AND student.role = 1
    AND student.deleted = 0
ON DUPLICATE KEY UPDATE
    status = 1,
    review_comment = '测试数据：审核通过',
    reviewed_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP;

-- 按真实审核通过记录重新计算测试课程的已选人数，脚本可重复执行。
UPDATE tb_course course
SET course.enrolled_count = (
    SELECT COUNT(*)
    FROM tb_enrollment enrollment
    WHERE enrollment.course_id = course.id
      AND enrollment.status = 1
)
WHERE course.id IN (9101, 9102, 9105, 9201, 9301, 9401);

COMMIT;

SELECT
    student.id AS student_id,
    student.username,
    course.code,
    course.name,
    course.category,
    enrollment.status
FROM tb_enrollment enrollment
JOIN tb_user student ON student.id = enrollment.student_id
JOIN tb_course course ON course.id = enrollment.course_id
WHERE student.username = 'student_liu'
  AND enrollment.status = 1
ORDER BY course.category, course.id;
