USE edu_platform;

-- 为已经初始化过的数据库补齐考试编辑功能依赖的字段和题目表。
DROP PROCEDURE IF EXISTS add_exam_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_exam_column_if_missing(
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_exam'
          AND COLUMN_NAME = target_column
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE tb_exam ADD COLUMN `', target_column, '` ', column_definition
        );
        PREPARE statement_to_run FROM @ddl;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

CALL add_exam_column_if_missing(
    'duration',
    'INT NOT NULL DEFAULT 60 COMMENT ''考试时长（分钟）'' AFTER `full_score`'
);
CALL add_exam_column_if_missing(
    'max_attempts',
    'INT NOT NULL DEFAULT 1 COMMENT ''允许提交次数'' AFTER `duration`'
);

DROP PROCEDURE IF EXISTS add_exam_column_if_missing;

CREATE TABLE IF NOT EXISTS tb_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID，对应 tb_exam.id',
    type TINYINT NOT NULL COMMENT '题型：0=选择题，1=填空题',
    title TEXT NOT NULL COMMENT '题目内容',
    options TEXT NULL COMMENT '选择题选项，JSON 数组',
    answer TEXT NULL COMMENT '正确答案',
    score INT NOT NULL DEFAULT 10 COMMENT '题目分值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '题目顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_question_exam_sort (exam_id, sort_order),
    CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES tb_exam(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试题目表';

SELECT exam.id, exam.title, exam.duration, COUNT(question.id) AS question_count
FROM tb_exam exam
LEFT JOIN tb_question question ON question.exam_id = exam.id
WHERE exam.deleted = 0
GROUP BY exam.id, exam.title, exam.duration
ORDER BY exam.id;
