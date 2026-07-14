USE edu_platform;
SET NAMES utf8mb4;

-- Update existing exams with duration and fix garbled names
UPDATE tb_exam SET duration = 60, title = 'Java Web 阶段测验' WHERE id = 1;
UPDATE tb_exam SET duration = 90, title = '数据库设计小测' WHERE id = 2;
UPDATE tb_exam SET duration = 45, title = 'MyBatis Plus 基础测验' WHERE id = 3;
UPDATE tb_exam SET duration = 60, title = 'Python 数据分析测验' WHERE id = 4;

-- Insert exam 1 questions (Java Web 阶段测验 - 选择题+填空题混合)
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(1, 0, 'Spring Boot 中，以下哪个注解用于启动自动配置？', '["@EnableAutoConfiguration","@SpringBootApplication","@Configuration","@ComponentScan"]', 'B', 10, 1),
(1, 0, 'HTTP 状态码 401 表示什么？', '["服务器内部错误","资源未找到","未授权（需要身份验证）","请求超时"]', 'C', 10, 2),
(1, 0, '以下哪个是 MyBatis Plus 的核心功能？', '["自动建表","代码生成器","分布式事务","服务网格"]', 'B', 10, 3),
(1, 0, 'JWT 中 Token 通常存放在 HTTP 请求的哪个头部？', '["Authorization","Content-Type","Accept","Cookie"]', 'A', 10, 4),
(1, 1, 'Spring Boot 默认内嵌的 Web 服务器是 ______。', NULL, 'Tomcat', 10, 5),
(1, 1, 'MyBatis Plus 中，逻辑删除的全局配置属性是 ______。', NULL, 'logic-delete-field', 10, 6);

-- Insert exam 3 questions (MyBatis Plus 基础测验 - 全选择题)
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(3, 0, 'MyBatis Plus 中，以下哪个注解用于标识主键？', '["@TableId","@TableField","@TableName","@Key"]', 'A', 20, 1),
(3, 0, 'BaseMapper 接口中，用于根据 ID 查询的方法是？', '["selectOne","selectById","getById","findById"]', 'B', 20, 2),
(3, 0, 'MyBatis Plus 的条件构造器类是？', '["QueryWrapper","ConditionBuilder","SqlBuilder","CriteriaQuery"]', 'A', 20, 3),
(3, 0, '以下哪个 MyBatis Plus 功能可以实现自动填充时间字段？', '["MetaObjectHandler","TimeHandler","FieldFill","AutoDate"]', 'A', 20, 4),
(3, 0, 'MyBatis Plus 的分页插件是？', '["PaginationInterceptor","PageHelper","MyBatisPage","DataPager"]', 'A', 20, 5);

-- Insert exam 4 questions (Python 数据分析测验)
INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(4, 0, 'Pandas 中，读取 CSV 文件的函数是？', '["read_excel","read_csv","load_csv","import_csv"]', 'B', 12, 1),
(4, 0, 'NumPy 中，创建全零数组的函数是？', '["ones","zeros","empty","full"]', 'B', 12, 2),
(4, 0, 'Pandas 中，用于删除缺失值的函数是？', '["fillna","dropna","remove_na","delete_na"]', 'B', 12, 3),
(4, 0, '以下哪个库主要用于数据可视化？', '["Pandas","NumPy","Matplotlib","Scipy"]', 'C', 12, 4),
(4, 1, 'Pandas 中，按列筛选的语法是 df[______]？', NULL, '列名', 12, 5),
(4, 1, 'NumPy 数组的维度属性是 ______。', NULL, 'shape', 12, 6),
(4, 1, 'Pandas 中，分组聚合的函数是 ______。', NULL, 'groupby', 14, 7),
(4, 1, 'Matplotlib 中，显示图形的函数是 plt.______()。', NULL, 'show', 14, 8);
