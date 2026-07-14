USE edu_platform;
SET NAMES utf8mb4;

-- ===== 新增考试 =====

-- 数据库设计小测（已有，补充题目）
UPDATE tb_exam SET duration = 60, status = 1, title = '数据库设计小测' WHERE id = 2;

INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(2, 0, '数据库设计中，以下哪个是逻辑设计的产物？', '["E-R图","关系模式","物理存储结构","索引文件"]', 'B', 20, 1),
(2, 0, 'MySQL 中，以下哪个存储引擎支持事务？', '["MyISAM","InnoDB","Memory","Archive"]', 'B', 20, 2),
(2, 0, '以下哪个是索引的类型？', '["B+树索引","链表索引","栈索引","队列索引"]', 'A', 20, 3),
(2, 1, '数据库事务的四个特性简称 ______。', NULL, 'ACID', 20, 4),
(2, 1, 'MyBatis Plus 中，逻辑删除的字段值 1 表示 ______。', NULL, '已删除', 20, 5);

-- Spring Boot 微服务开发 - 期中考试 (course 9104, teacher 1000)
INSERT INTO tb_exam (course_id, teacher_id, title, description, start_time, end_time, full_score, duration, status) VALUES
(9104, 1000, 'Spring Boot 微服务期中考试', '覆盖 Spring Boot 核心配置、自动装配和微服务基础。', '2026-09-15 09:00:00', '2026-09-15 10:30:00', 100, 90, 1);

SET @exam5 = LAST_INSERT_ID();

INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(@exam5, 0, 'Spring Boot 的默认配置文件是？', '["application.properties","spring.xml","web.xml","config.yml"]', 'A', 10, 1),
(@exam5, 0, '以下哪个注解用于将类标记为配置类？', '["@Component","@Service","@Configuration","@Bean"]', 'C', 10, 2),
(@exam5, 0, 'Spring Boot 内嵌的 Tomcat 默认端口是？', '["8080","80","8443","9090"]', 'A', 10, 3),
(@exam5, 0, 'Actuator 的健康检查端点路径是？', '["/health","/info","/metrics","/status"]', 'A', 10, 4),
(@exam5, 0, '以下哪个是 Spring Cloud 的服务注册中心？', '["Nacos","Redis","RabbitMQ","Elasticsearch"]', 'A', 10, 5),
(@exam5, 1, 'Spring Boot 中，读取配置文件的注解是 @______。', NULL, 'Value', 10, 6),
(@exam5, 1, 'Spring Boot 的自动配置核心注解是 @______。', NULL, 'EnableAutoConfiguration', 10, 7),
(@exam5, 1, '将 Spring Boot 应用打包成可执行 JAR 的插件是 ______。', NULL, 'spring-boot-maven-plugin', 10, 8),
(@exam5, 1, '微服务架构中，API 网关常用的功能是 ______ 和路由。', NULL, '负载均衡', 10, 9),
(@exam5, 1, 'Spring Cloud 中，负载均衡的客户端是 ______。', NULL, 'Ribbon', 10, 10);

-- 数据结构与算法 - 期中考试 (course 9105, teacher 1000)
INSERT INTO tb_exam (course_id, teacher_id, title, description, start_time, end_time, full_score, duration, status) VALUES
(9105, 1000, '数据结构期中测验', '线性表、栈、队列和树的基础知识。', '2026-10-10 14:00:00', '2026-10-10 15:30:00', 100, 90, 1);

SET @exam6 = LAST_INSERT_ID();

INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(@exam6, 0, '栈的运算特点是？', '["先进先出","先进后出","随机访问","双端进出"]', 'B', 10, 1),
(@exam6, 0, '以下哪种数据结构适合实现广度优先搜索？', '["栈","队列","数组","链表"]', 'B', 10, 2),
(@exam6, 0, '二叉树中，度为 2 的节点数比叶子节点数少？', '["0","1","2","不确定"]', 'B', 10, 3),
(@exam6, 0, '快速排序的平均时间复杂度是？', '["O(n)","O(nlogn)","O(n²)","O(logn)"]', 'B', 10, 4),
(@exam6, 0, '哈希表解决冲突的方法不包括？', '["链地址法","开放地址法","再哈希法","冒泡法"]', 'D', 10, 5),
(@exam6, 1, '线性表的顺序存储结构称为 ______。', NULL, '顺序表', 10, 6),
(@exam6, 1, '完全二叉树可以用 ______ 存储结构实现。', NULL, '顺序', 10, 7),
(@exam6, 1, '图的深度优先遍历使用的辅助数据结构是 ______。', NULL, '栈', 10, 8),
(@exam6, 1, '折半查找要求线性表必须采用 ______ 存储结构。', NULL, '顺序', 10, 9),
(@exam6, 1, '最小生成树的经典算法有 Prim 和 ______。', NULL, 'Kruskal', 10, 10);

-- Vue3 前端开发 - 期中考试 (course 9202, teacher 1001)
INSERT INTO tb_exam (course_id, teacher_id, title, description, start_time, end_time, full_score, duration, status) VALUES
(9202, 1001, 'Vue3 基础测验', 'Vue3 核心语法、组合式 API 和路由基础。', '2026-09-20 10:00:00', '2026-09-20 11:00:00', 100, 60, 1);

SET @exam7 = LAST_INSERT_ID();

INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(@exam7, 0, 'Vue3 中，以下哪个是组合式 API 的入口函数？', '["setup","data","created","mounted"]', 'A', 10, 1),
(@exam7, 0, 'Vue3 中，创建响应式变量的函数是？', '["ref","reactive","computed","watch"]', 'A', 10, 2),
(@exam7, 0, 'Vue Router 中，用于导航到新路径的方法是？', '["push","go","link","navigate"]', 'A', 10, 3),
(@exam7, 0, '以下哪个是 Vue3 的状态管理库？', '["Pinia","Vuex","Redux","MobX"]', 'A', 10, 4),
(@exam7, 0, 'Vue3 中，侦听器函数是？', '["watch","watchEffect","computed","onMounted"]', 'A', 10, 5),
(@exam7, 1, 'Vue3 中，模板循环渲染的指令是 v-______。', NULL, 'for', 10, 6),
(@exam7, 1, 'Vue3 组件间传递数据使用 ______。', NULL, 'props', 10, 7),
(@exam7, 1, 'Vue3 中，获取 DOM 元素的特殊属性是 ref 和 ______。', NULL, 'template ref', 8, 8),
(@exam7, 1, 'Vite 的开发服务器默认端口是 ______。', NULL, '5173', 8, 9),
(@exam7, 1, 'Element Plus 的表格组件是 el-______。', NULL, 'table', 12, 10);

-- 人工智能基础 - 期中考试 (course 9203, teacher 1001)
INSERT INTO tb_exam (course_id, teacher_id, title, description, start_time, end_time, full_score, duration, status) VALUES
(9203, 1001, '人工智能基础测验', 'AI 发展史、机器学习和深度学习入门。', '2026-10-05 14:00:00', '2026-10-05 15:00:00', 100, 60, 1);

SET @exam8 = LAST_INSERT_ID();

INSERT INTO tb_question (exam_id, type, title, options, answer, score, sort_order) VALUES
(@exam8, 0, '以下哪个是监督学习的典型任务？', '["分类","聚类","降维","关联规则"]', 'A', 12, 1),
(@exam8, 0, '线性回归的损失函数通常使用？', '["均方误差","交叉熵","Hinge损失","余弦相似度"]', 'A', 12, 2),
(@exam8, 0, '以下哪个算法是集成学习方法？', '["随机森林","KNN","K-Means","PCA"]', 'A', 12, 3),
(@exam8, 0, '神经网络的激活函数中，哪个可以将输出压缩到 (0,1)？', '["Sigmoid","ReLU","Tanh","Leaky ReLU"]', 'A', 12, 4),
(@exam8, 0, '以下哪个是无监督学习算法？', '["K-Means","线性回归","逻辑回归","支持向量机"]', 'A', 12, 5),
(@exam8, 1, '机器学习中，防止过拟合的技术称为 ______。', NULL, '正则化', 10, 6),
(@exam8, 1, '决策树中衡量不纯度的指标有信息增益和 ______。', NULL, '基尼指数', 10, 7),
(@exam8, 1, 'CNN 中用于缩小特征图尺寸的操作是 ______。', NULL, '池化', 10, 8),
(@exam8, 1, 'RNN 主要用于处理 ______ 数据。', NULL, '序列', 10, 9);
