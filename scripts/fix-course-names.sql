USE edu_platform;
SET NAMES utf8mb4;

UPDATE tb_course SET name = 'Java Web 开发实战', description = '围绕 Spring Boot、网关、接口联调完成在线教育系统后端开发。', dept = '计算机科学与技术学院', category = '必修', tags = '核心课' WHERE id = 9101;

UPDATE tb_course SET name = '数据库系统原理', description = '学习核心业务表设计、实体映射、逻辑删除和常用 CRUD。', dept = '计算机科学与技术学院', category = '必修', tags = '核心课' WHERE id = 9102;

UPDATE tb_course SET name = 'MyBatis Plus 企业开发', description = '深入学习 MyBatis Plus 高级特性和企业级应用。', dept = '软件工程系', category = '必修', tags = '核心课' WHERE id = 9103;

UPDATE tb_course SET name = 'Spring Boot 微服务开发', description = '从零搭建微服务架构，掌握服务注册与发现、网关路由。', dept = '软件工程系', category = '必修', tags = '核心课' WHERE id = 9104;

UPDATE tb_course SET name = '数据结构与算法', description = '系统学习常用数据结构和经典算法。', dept = '计算机科学与技术学院', category = '必修', tags = '核心课' WHERE id = 9105;

UPDATE tb_course SET name = '操作系统', description = '进程管理、内存管理、文件系统等核心概念。', dept = '计算机科学与技术学院', category = '必修', tags = '核心课' WHERE id = 9106;

UPDATE tb_course SET name = '计算机网络', description = 'TCP/IP 协议栈、HTTP、网络编程。', dept = '计算机科学与技术学院', category = '必修', tags = '核心课' WHERE id = 9107;

UPDATE tb_course SET name = '软件工程', description = '软件开发生命周期、需求分析、设计模式。', dept = '软件工程系', category = '必修', tags = '核心课' WHERE id = 9108;

UPDATE tb_course SET name = '编译原理', description = '词法分析、语法分析、语义分析和代码生成。', dept = '软件工程系', category = '必修', tags = '专业课' WHERE id = 9109;

UPDATE tb_course SET name = 'Linux 系统管理', description = 'Linux 基础命令、Shell 编程和服务管理。', dept = '软件工程系', category = '必修', tags = '实践课' WHERE id = 9110;

UPDATE tb_course SET name = 'Python 数据分析', description = '使用 Pandas、NumPy 进行数据处理和分析。', dept = '软件工程系', category = '选修', tags = '专业选修' WHERE id = 9201;

UPDATE tb_course SET name = 'Vue3 前端开发', description = 'Vue3、Vite、Pinia、Element Plus 构建前端页面。', dept = '软件工程系', category = '选修', tags = '专业选修' WHERE id = 9202;

UPDATE tb_course SET name = '人工智能基础', description = '机器学习、深度学习入门知识。', dept = '人工智能学院', category = '选修', tags = 'AI课程' WHERE id = 9203;

UPDATE tb_course SET name = '深度学习导论', description = 'CNN、RNN、Transformer 等深度学习模型。', dept = '人工智能学院', category = '选修', tags = 'AI课程' WHERE id = 9204;

UPDATE tb_course SET name = '云计算与容器技术', description = 'Docker、Kubernetes 等云原生技术。', dept = '软件工程系', category = '选修', tags = '专业选修' WHERE id = 9205;

UPDATE tb_course SET name = '大数据技术基础', description = 'Hadoop、Spark 等大数据处理框架。', dept = '计算机科学与技术学院', category = '选修', tags = '专业选修' WHERE id = 9206;

UPDATE tb_course SET name = '大学生心理健康教育', description = '关注大学生心理健康，提升心理素质。', dept = '马克思主义学院', category = '通识', tags = '通识课' WHERE id = 9301;

UPDATE tb_course SET name = '大学生职业发展与就业指导', description = '职业规划、简历制作和面试技巧。', dept = '学生工作部', category = '通识', tags = '通识课' WHERE id = 9302;

UPDATE tb_course SET name = '中国近现代史纲要', description = '学习中国近现代历史发展进程。', dept = '马克思主义学院', category = '通识', tags = '思政课' WHERE id = 9303;

UPDATE tb_course SET name = '大学英语（四）', description = '大学英语高级课程。', dept = '外国语学院', category = '通识', tags = '公共课' WHERE id = 9304;

UPDATE tb_course SET name = '体育（四）', description = '大学体育课程。', dept = '体育学院', category = '通识', tags = '公共课' WHERE id = 9305;

UPDATE tb_course SET name = '开源软件项目实践', description = '参与开源项目，学习协作开发。', dept = '软件工程系', category = '个性课程', tags = '创新实践' WHERE id = 9401;

UPDATE tb_course SET name = '企业级项目开发实训', description = '模拟企业项目开发流程。', dept = '软件工程系', category = '个性课程', tags = '实践课' WHERE id = 9402;

UPDATE tb_course SET name = '科技创新训练', description = '培养科技创新思维和能力。', dept = '创新创业学院', category = '个性课程', tags = '创新创业' WHERE id = 9403;

UPDATE tb_course SET name = 'ACM 程序设计竞赛训练', description = '算法竞赛训练，提高编程能力。', dept = '软件工程系', category = '个性课程', tags = '创新实践' WHERE id = 9404;

UPDATE tb_course SET name = '毕业设计（软件工程）', description = '软件工程专业毕业设计。', dept = '软件工程系', category = '个性课程', tags = '毕业实践' WHERE id = 9405;
