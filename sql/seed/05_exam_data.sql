USE edu_platform;

-- ============================================================
-- 考试种子数据：DDL 补充 + 考试 + 题目
-- 从以下文件合并：
--   add-max-attempts.sql / exam-db-changes.sql / fix-exam-times.sql
--   fix-max-attempts.sql / seed-exam-data.sql / seed-more-exams.sql
-- 脚本可重复执行，幂等安全。
-- ============================================================

-- ── 1. DDL：补充 tb_exam 列（幂等检查）───────────────────────
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

CALL add_exam_column_if_missing('duration',      'INT NOT NULL DEFAULT 60 COMMENT ''考试时长（分钟）'' AFTER `full_score`');
CALL add_exam_column_if_missing('max_attempts',  'INT NOT NULL DEFAULT 1 COMMENT ''允许提交次数'' AFTER `duration`');

DROP PROCEDURE IF EXISTS add_exam_column_if_missing;

-- ── 2. DDL：题目表 ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tb_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    type TINYINT NOT NULL COMMENT '0=选择题 1=填空题',
    title TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选择题选项(JSON数组)',
    answer TEXT COMMENT '正确答案',
    score INT NOT NULL DEFAULT 10,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_question_exam (exam_id),
    CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES tb_exam(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- ── 3. 全局考试设置 ──────────────────────────────────────────
UPDATE tb_exam SET max_attempts = 99 WHERE max_attempts <= 1;

-- ── 4. 考试数据 ──────────────────────────────────────────────

-- 基础 4 场考试（init.sql 不包含，seed-all-data.sql 会创建）
INSERT INTO tb_exam (id, course_id, teacher_id, title, description,
    start_time, end_time, full_score, duration, status)
VALUES
    (1, 9101, 101, 'Java Web 阶段测验',
     '覆盖登录、网关、课程和选课接口基础知识。',
     '2026-07-01 09:00:00', '2026-12-31 23:59:59', 100, 60, 1),
    (2, 9102, 101, '数据库设计小测',
     '考察核心业务表、索引和逻辑删除设计。',
     '2026-09-02 14:00:00', '2026-09-02 15:30:00', 100, 90, 1),
    (3, 9103, 101, 'MyBatis Plus 基础测验',
     '核心注解和 CRUD 操作。',
     '2026-09-10 19:00:00', '2026-09-10 20:00:00', 100, 45, 2),
    (4, 9201, 103, 'Python 数据分析测验',
     'Pandas 和 NumPy 基础。',
     '2026-08-28 10:00:00', '2026-08-28 11:30:00', 100, 60, 1)
ON DUPLICATE KEY UPDATE
    title = VALUES(title), duration = VALUES(duration),
    status = VALUES(status), teacher_id = VALUES(teacher_id),
    start_time = VALUES(start_time), end_time = VALUES(end_time);

-- 重新分配考试教师（匹配 seed_course_data 中的课程教师分配）
UPDATE tb_exam SET teacher_id = 101 WHERE id IN (1, 2, 3, 5, 6);
UPDATE tb_exam SET teacher_id = 103 WHERE id IN (4, 7, 8);

-- 新增考试：Spring Boot 微服务 (course 9104, teacher t1=101)
INSERT INTO tb_exam (id, course_id, teacher_id, title, description,
    start_time, end_time, full_score, duration, status)
VALUES
    (5, 9104, 101, 'Spring Boot 微服务期中考试',
     '覆盖 Spring Boot 核心配置、自动装配和微服务基础。',
     '2026-09-15 09:00:00', '2026-09-15 10:30:00', 100, 90, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), duration = VALUES(duration),
    status = VALUES(status), teacher_id = VALUES(teacher_id);

-- 新增考试：数据结构与算法 (course 9105, teacher t1=101)
INSERT INTO tb_exam (id, course_id, teacher_id, title, description,
    start_time, end_time, full_score, duration, status)
VALUES
    (6, 9105, 101, '数据结构期中测验',
     '线性表、栈、队列和树的基础知识。',
     '2026-10-10 14:00:00', '2026-10-10 15:30:00', 100, 90, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), duration = VALUES(duration),
    status = VALUES(status), teacher_id = VALUES(teacher_id);

-- 新增考试：Vue3 前端 (course 9202, teacher t3=103)
INSERT INTO tb_exam (id, course_id, teacher_id, title, description,
    start_time, end_time, full_score, duration, status)
VALUES
    (7, 9202, 103, 'Vue3 基础测验',
     'Vue3 核心语法、组合式 API 和路由基础。',
     '2026-09-20 10:00:00', '2026-09-20 11:00:00', 100, 60, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), duration = VALUES(duration),
    status = VALUES(status), teacher_id = VALUES(teacher_id);

-- 新增考试：AI 基础 (course 9203, teacher t3=103)
INSERT INTO tb_exam (id, course_id, teacher_id, title, description,
    start_time, end_time, full_score, duration, status)
VALUES
    (8, 9203, 103, '人工智能基础测验',
     'AI 发展史、机器学习和深度学习入门。',
     '2026-10-05 14:00:00', '2026-10-05 15:00:00', 100, 60, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), duration = VALUES(duration),
    status = VALUES(status), teacher_id = VALUES(teacher_id);

-- ── 5. 题目数据 ──────────────────────────────────────────────

-- === Exam 1: Java Web 阶段测验 ===
DELETE FROM tb_question WHERE exam_id = 1;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(1, 0, 'Spring Boot 中，以下哪个注解用于启动自动配置？',
    '["@EnableAutoConfiguration","@SpringBootApplication","@Configuration","@ComponentScan"]', 'B', 10, 1),
(1, 0, 'HTTP 状态码 401 表示什么？',
    '["服务器内部错误","资源未找到","未授权（需要身份验证）","请求超时"]', 'C', 10, 2),
(1, 0, '以下哪个是 MyBatis Plus 的核心功能？',
    '["自动建表","代码生成器","分布式事务","服务网格"]', 'B', 10, 3),
(1, 0, 'JWT 中 Token 通常存放在 HTTP 请求的哪个头部？',
    '["Authorization","Content-Type","Accept","Cookie"]', 'A', 10, 4),
(1, 1, 'Spring Boot 默认内嵌的 Web 服务器是 ______。', NULL, 'Tomcat', 10, 5),
(1, 1, 'MyBatis Plus 中，逻辑删除的全局配置属性是 ______。', NULL, 'logic-delete-field', 10, 6);

-- === Exam 2: 数据库设计小测 ===
DELETE FROM tb_question WHERE exam_id = 2;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(2, 0, '数据库设计中，以下哪个是逻辑设计的产物？',
    '["E-R图","关系模式","物理存储结构","索引文件"]', 'B', 20, 1),
(2, 0, 'MySQL 中，以下哪个存储引擎支持事务？',
    '["MyISAM","InnoDB","Memory","Archive"]', 'B', 20, 2),
(2, 0, '以下哪个是索引的类型？',
    '["B+树索引","链表索引","栈索引","队列索引"]', 'A', 20, 3),
(2, 1, '数据库事务的四个特性简称 ______。', NULL, 'ACID', 20, 4),
(2, 1, 'MyBatis Plus 中，逻辑删除的字段值 1 表示 ______。', NULL, '已删除', 20, 5);

-- === Exam 3: MyBatis Plus 基础测验 ===
DELETE FROM tb_question WHERE exam_id = 3;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(3, 0, 'MyBatis Plus 中，以下哪个注解用于标识主键？',
    '["@TableId","@TableField","@TableName","@Key"]', 'A', 20, 1),
(3, 0, 'BaseMapper 接口中，用于根据 ID 查询的方法是？',
    '["selectOne","selectById","getById","findById"]', 'B', 20, 2),
(3, 0, 'MyBatis Plus 的条件构造器类是？',
    '["QueryWrapper","ConditionBuilder","SqlBuilder","CriteriaQuery"]', 'A', 20, 3),
(3, 0, '以下哪个 MyBatis Plus 功能可以实现自动填充时间字段？',
    '["MetaObjectHandler","TimeHandler","FieldFill","AutoDate"]', 'A', 20, 4),
(3, 0, 'MyBatis Plus 的分页插件是？',
    '["PaginationInterceptor","PageHelper","MyBatisPage","DataPager"]', 'A', 20, 5);

-- === Exam 4: Python 数据分析测验 ===
DELETE FROM tb_question WHERE exam_id = 4;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(4, 0, 'Pandas 中，读取 CSV 文件的函数是？',
    '["read_excel","read_csv","load_csv","import_csv"]', 'B', 12, 1),
(4, 0, 'NumPy 中，创建全零数组的函数是？',
    '["ones","zeros","empty","full"]', 'B', 12, 2),
(4, 0, 'Pandas 中，用于删除缺失值的函数是？',
    '["fillna","dropna","remove_na","delete_na"]', 'B', 12, 3),
(4, 0, '以下哪个库主要用于数据可视化？',
    '["Pandas","NumPy","Matplotlib","Scipy"]', 'C', 12, 4),
(4, 1, 'Pandas 中，按列筛选的语法是 df[______]？', NULL, '列名', 12, 5),
(4, 1, 'NumPy 数组的维度属性是 ______。', NULL, 'shape', 12, 6),
(4, 1, 'Pandas 中，分组聚合的函数是 ______。', NULL, 'groupby', 14, 7),
(4, 1, 'Matplotlib 中，显示图形的函数是 plt.______()。', NULL, 'show', 14, 8);

-- === Exam 5: Spring Boot 微服务期中考试 ===
DELETE FROM tb_question WHERE exam_id = 5;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(5, 0, 'Spring Boot 的默认配置文件是？',
    '["application.properties","spring.xml","web.xml","config.yml"]', 'A', 10, 1),
(5, 0, '以下哪个注解用于将类标记为配置类？',
    '["@Component","@Service","@Configuration","@Bean"]', 'C', 10, 2),
(5, 0, 'Spring Boot 内嵌的 Tomcat 默认端口是？',
    '["8080","80","8443","9090"]', 'A', 10, 3),
(5, 0, 'Actuator 的健康检查端点路径是？',
    '["/health","/info","/metrics","/status"]', 'A', 10, 4),
(5, 0, '以下哪个是 Spring Cloud 的服务注册中心？',
    '["Nacos","Redis","RabbitMQ","Elasticsearch"]', 'A', 10, 5),
(5, 1, 'Spring Boot 中，读取配置文件的注解是 @______。', NULL, 'Value', 10, 6),
(5, 1, 'Spring Boot 的自动配置核心注解是 @______。', NULL, 'EnableAutoConfiguration', 10, 7),
(5, 1, '将 Spring Boot 应用打包成可执行 JAR 的插件是 ______。', NULL, 'spring-boot-maven-plugin', 10, 8),
(5, 1, '微服务架构中，API 网关常用的功能是 ______ 和路由。', NULL, '负载均衡', 10, 9),
(5, 1, 'Spring Cloud 中，负载均衡的客户端是 ______。', NULL, 'Ribbon', 10, 10);

-- === Exam 6: 数据结构期中测验 ===
DELETE FROM tb_question WHERE exam_id = 6;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(6, 0, '栈的运算特点是？',
    '["先进先出","先进后出","随机访问","双端进出"]', 'B', 10, 1),
(6, 0, '以下哪种数据结构适合实现广度优先搜索？',
    '["栈","队列","数组","链表"]', 'B', 10, 2),
(6, 0, '二叉树中，度为 2 的节点数比叶子节点数少？',
    '["0","1","2","不确定"]', 'B', 10, 3),
(6, 0, '快速排序的平均时间复杂度是？',
    '["O(n)","O(nlogn)","O(n²)","O(logn)"]', 'B', 10, 4),
(6, 0, '哈希表解决冲突的方法不包括？',
    '["链地址法","开放地址法","再哈希法","冒泡法"]', 'D', 10, 5),
(6, 1, '线性表的顺序存储结构称为 ______。', NULL, '顺序表', 10, 6),
(6, 1, '完全二叉树可以用 ______ 存储结构实现。', NULL, '顺序', 10, 7),
(6, 1, '图的深度优先遍历使用的辅助数据结构是 ______。', NULL, '栈', 10, 8),
(6, 1, '折半查找要求线性表必须采用 ______ 存储结构。', NULL, '顺序', 10, 9),
(6, 1, '最小生成树的经典算法有 Prim 和 ______。', NULL, 'Kruskal', 10, 10);

-- === Exam 7: Vue3 基础测验 ===
DELETE FROM tb_question WHERE exam_id = 7;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(7, 0, 'Vue3 中，以下哪个是组合式 API 的入口函数？',
    '["setup","data","created","mounted"]', 'A', 10, 1),
(7, 0, 'Vue3 中，创建响应式变量的函数是？',
    '["ref","reactive","computed","watch"]', 'A', 10, 2),
(7, 0, 'Vue Router 中，用于导航到新路径的方法是？',
    '["push","go","link","navigate"]', 'A', 10, 3),
(7, 0, '以下哪个是 Vue3 的状态管理库？',
    '["Pinia","Vuex","Redux","MobX"]', 'A', 10, 4),
(7, 0, 'Vue3 中，侦听器函数是？',
    '["watch","watchEffect","computed","onMounted"]', 'A', 10, 5),
(7, 1, 'Vue3 中，模板循环渲染的指令是 v-______。', NULL, 'for', 10, 6),
(7, 1, 'Vue3 组件间传递数据使用 ______。', NULL, 'props', 10, 7),
(7, 1, 'Vue3 中，获取 DOM 元素的特殊属性是 ref 和 ______。', NULL, 'template ref', 8, 8),
(7, 1, 'Vite 的开发服务器默认端口是 ______。', NULL, '5173', 8, 9),
(7, 1, 'Element Plus 的表格组件是 el-______。', NULL, 'table', 12, 10);

-- === Exam 8: 人工智能基础测验 ===
DELETE FROM tb_question WHERE exam_id = 8;
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(8, 0, '以下哪个是监督学习的典型任务？',
    '["分类","聚类","降维","关联规则"]', 'A', 12, 1),
(8, 0, '线性回归的损失函数通常使用？',
    '["均方误差","交叉熵","Hinge损失","余弦相似度"]', 'A', 12, 2),
(8, 0, '以下哪个算法是集成学习方法？',
    '["随机森林","KNN","K-Means","PCA"]', 'A', 12, 3),
(8, 0, '神经网络的激活函数中，哪个可以将输出压缩到 (0,1)？',
    '["Sigmoid","ReLU","Tanh","Leaky ReLU"]', 'A', 12, 4),
(8, 0, '以下哪个是无监督学习算法？',
    '["K-Means","线性回归","逻辑回归","支持向量机"]', 'A', 12, 5),
(8, 1, '机器学习中，防止过拟合的技术称为 ______。', NULL, '正则化', 10, 6),
(8, 1, '决策树中衡量不纯度的指标有信息增益和 ______。', NULL, '基尼指数', 10, 7),
(8, 1, 'CNN 中用于缩小特征图尺寸的操作是 ______。', NULL, '池化', 10, 8),
(8, 1, 'RNN 主要用于处理 ______ 数据。', NULL, '序列', 10, 9);

-- ── 6. 验证 ──────────────────────────────────────────────────
SELECT e.id, e.title, c.name AS course_name, u.nickname AS teacher,
       e.duration, e.max_attempts, e.status,
       COUNT(q.id) AS question_count
FROM tb_exam e
JOIN tb_course c ON c.id = e.course_id
JOIN tb_user u ON u.id = e.teacher_id
LEFT JOIN tb_question q ON q.exam_id = e.id
WHERE e.deleted = 0
GROUP BY e.id, e.title, c.name, u.nickname, e.duration, e.max_attempts, e.status
ORDER BY e.id;
