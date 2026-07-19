USE edu_platform;

-- 1. 先删外键
ALTER TABLE tb_enrollment DROP FOREIGN KEY fk_enrollment_course;

-- 2. 再删依赖外键的索引
ALTER TABLE tb_enrollment DROP INDEX idx_enrollment_course_status;

-- 3. 清理重复数据
DELETE e1 FROM tb_enrollment e1
INNER JOIN tb_enrollment e2
WHERE e1.id > e2.id
  AND e1.class_id = e2.class_id
  AND e1.student_id = e2.student_id
  AND e1.status IN (0, 1);

DELETE e1 FROM tb_enrollment e1
INNER JOIN tb_enrollment e2
WHERE e1.id > e2.id
  AND e1.course_id = e2.course_id
  AND e1.student_id = e2.student_id
  AND e1.status IN (0, 1);

-- 4. 添加新约束
ALTER TABLE tb_enrollment
    ADD UNIQUE KEY uk_enrollment_class_student (class_id, student_id),
    ADD UNIQUE KEY uk_enrollment_course_student (course_id, student_id),
    ADD INDEX idx_enrollment_class_status (class_id, status),
    ADD CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES tb_course_class(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    ADD CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES tb_course(id)
        ON UPDATE CASCADE ON DELETE RESTRICT;
