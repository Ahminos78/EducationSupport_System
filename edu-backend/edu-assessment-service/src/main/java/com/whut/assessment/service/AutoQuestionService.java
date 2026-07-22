package com.whut.assessment.service;

import com.whut.assessment.client.AiExamClient;
import com.whut.assessment.dto.AiGenerateQuestionsRequest;
import com.whut.assessment.mapper.ExamMapper;
import com.whut.assessment.vo.QuestionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AutoQuestionService {

    private final ExamMapper examMapper;
    private final AiExamClient aiExamClient;

    private static final List<QuestionTemplate> COMMON_TEMPLATES = List.of(
            new QuestionTemplate(true, "在Java中，以下哪个关键字用于实现接口？", new String[]{"extends", "implements", "abstract", "interface"}, "B"),
            new QuestionTemplate(true, "以下哪个是面向对象的三大特性之一？", new String[]{"封装", "继承", "多态", "以上都是"}, "D"),
            new QuestionTemplate(true, "HTTP 状态码 200 表示什么？", new String[]{"请求成功", "重定向", "客户端错误", "服务器错误"}, "A"),
            new QuestionTemplate(true, "以下哪个是关系型数据库？", new String[]{"MySQL", "Redis", "MongoDB", "Elasticsearch"}, "A"),
            new QuestionTemplate(true, "Git 中用于切换分支的命令是？", new String[]{"git branch", "git checkout", "git switch", "git clone"}, "B"),
            new QuestionTemplate(true, "以下哪个设计模式属于创建型模式？", new String[]{"单例模式", "观察者模式", "适配器模式", "装饰器模式"}, "A"),
            new QuestionTemplate(true, "RESTful API 中，用于更新资源的 HTTP 方法是？", new String[]{"GET", "POST", "PUT", "DELETE"}, "C"),
            new QuestionTemplate(true, "以下哪个是 NoSQL 数据库？", new String[]{"PostgreSQL", "Oracle", "MongoDB", "SQLite"}, "C"),
            new QuestionTemplate(true, "JSON 的全称是什么？", new String[]{"Java Standard Object Notation", "JavaScript Object Notation", "Java Serialized Object Notation", "JavaScript Online Notation"}, "B"),
            new QuestionTemplate(false, "Spring Boot 的核心配置文件是 application.______。", null, "properties"),
            new QuestionTemplate(false, "Docker 的镜像仓库被称为 Docker ______。", null, "Hub"),
            new QuestionTemplate(false, "数据库事务的四个特性缩写是 ______。", null, "ACID"),
            new QuestionTemplate(false, "Linux 中查看当前目录的命令是 ______。", null, "pwd"),
            new QuestionTemplate(false, "MyBatis 中用于执行 SQL 映射的对象是 ______。", null, "SqlSession"),
            new QuestionTemplate(false, "Maven 中用于清理项目的命令是 mvn ______。", null, "clean"),
            new QuestionTemplate(false, "Vue 中用于双向绑定的指令是 v-______。", null, "model"),
            new QuestionTemplate(false, "HTTP 默认端口号是 ______。", null, "80"),
            new QuestionTemplate(false, "HTTPS 默认端口号是 ______。", null, "443")
    );

    private static final Map<String, List<QuestionTemplate>> TOPIC_TEMPLATES = Map.ofEntries(
            Map.entry("spring", List.of(
                    new QuestionTemplate(true, "Spring Boot 的默认配置文件格式是？", new String[]{"properties", "yml", "xml", "json"}, "A"),
                    new QuestionTemplate(true, "以下哪个是 Spring 的核心容器？", new String[]{"ApplicationContext", "BeanFactory", "ServletContext", "WebApplicationContext"}, "A"),
                    new QuestionTemplate(true, "@Autowired 注解的作用是？", new String[]{"自动装配", "自动配置", "自动扫描", "自动注入"}, "A"),
                    new QuestionTemplate(false, "Spring Boot 内嵌的 Web 服务器是 ______。", null, "Tomcat"),
                    new QuestionTemplate(false, "Spring 中用于声明事务的注解是 @______。", null, "Transactional")
            )),
            Map.entry("mybatis", List.of(
                    new QuestionTemplate(true, "MyBatis 中 #{} 和 ${} 的区别是？", new String[]{"#{}防注入，${}不防", "${}防注入，#{}不防", "没有区别", "都是字符串拼接"}, "A"),
                    new QuestionTemplate(true, "MyBatis Plus 的通用 CRUD 接口是？", new String[]{"BaseMapper", "CommonMapper", "CrudMapper", "EntityMapper"}, "A"),
                    new QuestionTemplate(false, "MyBatis 的映射文件后缀是 .______。", null, "xml"),
                    new QuestionTemplate(false, "MyBatis Plus 中分页查询使用的类是 ______。", null, "Page")
            )),
            Map.entry("数据库", List.of(
                    new QuestionTemplate(true, "以下哪个是 SQL 的聚合函数？", new String[]{"COUNT", "LENGTH", "TRIM", "CONCAT"}, "A"),
                    new QuestionTemplate(true, "数据库范式中，2NF 的要求是？", new String[]{"消除非主属性对候选键的部分依赖", "消除传递依赖", "属性不可再分", "以上都是"}, "A"),
                    new QuestionTemplate(false, "MySQL 的默认端口号是 ______。", null, "3306"),
                    new QuestionTemplate(false, "DELETE 语句删除数据后可以用 ______ 恢复。", null, "ROLLBACK")
            )),
            Map.entry("vue", List.of(
                    new QuestionTemplate(true, "Vue 3 的响应式 API 中，ref 和 reactive 的区别是？", new String[]{"ref用于基本类型，reactive用于对象", "ref用于对象，reactive用于基本类型", "两者没有区别", "ref是reactive的别名"}, "A"),
                    new QuestionTemplate(true, "Vue Router 中用于导航到新页面的方法是？", new String[]{"router.push", "router.go", "router.link", "router.navigate"}, "A"),
                    new QuestionTemplate(false, "Vue 3 的组合式 API 入口函数是 ______。", null, "setup"),
                    new QuestionTemplate(false, "Vite 构建工具默认的开发服务器端口是 ______。", null, "5173")
            )),
            Map.entry("python", List.of(
                    new QuestionTemplate(true, "Python 中列表的符号是？", new String[]{"[]", "{}", "()", "<>"}, "A"),
                    new QuestionTemplate(true, "Pandas 中读取 CSV 文件的函数是？", new String[]{"read_csv", "load_csv", "import_csv", "open_csv"}, "A"),
                    new QuestionTemplate(false, "Python 中定义函数的关键字是 ______。", null, "def"),
                    new QuestionTemplate(false, "NumPy 中创建全零数组的函数是 ______。", null, "zeros")
            )),
            Map.entry("ai", List.of(
                    new QuestionTemplate(true, "以下哪个是监督学习的算法？", new String[]{"线性回归", "K-Means", "PCA", "Apriori"}, "A"),
                    new QuestionTemplate(true, "神经网络中激活函数 Sigmoid 的输出范围是？", new String[]{"(0,1)", "(-1,1)", "(0,∞)", "(-∞,∞)"}, "A"),
                    new QuestionTemplate(false, "CNN 中用于缩小特征图的操作是 ______。", null, "池化"),
                    new QuestionTemplate(false, "RNN 主要用于处理 ______ 数据。", null, "序列")
            )),
            Map.entry("linux", List.of(
                    new QuestionTemplate(true, "Linux 中查看文件内容的命令是？", new String[]{"cat", "dir", "type", "show"}, "A"),
                    new QuestionTemplate(true, "Linux 中修改文件权限的命令是？", new String[]{"chmod", "chown", "chgrp", "chattr"}, "A"),
                    new QuestionTemplate(false, "Linux 中创建目录的命令是 mk______。", null, "dir"),
                    new QuestionTemplate(false, "查看 Linux 系统进程的命令是 ______。", null, "ps")
            )),
            Map.entry("数据结构", List.of(
                    new QuestionTemplate(true, "栈的运算特点是？", new String[]{"先进先出", "先进后出", "随机访问", "双端进出"}, "B"),
                    new QuestionTemplate(true, "二分查找的时间复杂度是？", new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"}, "B"),
                    new QuestionTemplate(false, "完全二叉树可以用 ______ 存储结构实现。", null, "顺序"),
                    new QuestionTemplate(false, "图的深度优先遍历使用的数据结构是 ______。", null, "栈")
            )),
            Map.entry("网络", List.of(
                    new QuestionTemplate(true, "TCP 协议位于 OSI 模型的哪一层？", new String[]{"网络层", "传输层", "会话层", "应用层"}, "B"),
                    new QuestionTemplate(true, "IP 地址 127.0.0.1 表示什么？", new String[]{"广播地址", "回环地址", "网关地址", "子网地址"}, "B"),
                    new QuestionTemplate(false, "HTTP 协议基于 ______ 协议。", null, "TCP"),
                    new QuestionTemplate(false, "DNS 的作用是将域名解析为 ______。", null, "IP地址")
            ))
    );

    public AutoQuestionService(ExamMapper examMapper, AiExamClient aiExamClient) {
        this.examMapper = examMapper;
        this.aiExamClient = aiExamClient;
    }

    public List<QuestionResponse> autoGenerate(Long courseId, int count) {
        String courseName = getCourseName(courseId);

        List<QuestionResponse> aiQuestions = aiExamClient.generateQuestions(
                new AiGenerateQuestionsRequest(courseName, "", Math.min(count, 20)));
        if (!aiQuestions.isEmpty()) {
            int order = 1;
            for (QuestionResponse q : aiQuestions) {
                q.setSortOrder(order++);
                if (q.getScore() == null || q.getScore() <= 0) q.setScore(10);
            }
            return aiQuestions;
        }

        return templateGenerate(courseName, count);
    }

    private List<QuestionResponse> templateGenerate(String courseName, int count) {
        String keyword = extractKeyword(courseName);
        List<QuestionTemplate> matched = findTemplates(keyword);
        List<QuestionTemplate> pool = new ArrayList<>();
        pool.addAll(matched);
        int commonNeeded = Math.max(count - pool.size(), 0);
        for (int i = 0; i < Math.min(commonNeeded, COMMON_TEMPLATES.size()); i++) {
            pool.add(COMMON_TEMPLATES.get(i));
        }
        List<QuestionResponse> result = new ArrayList<>();
        int order = 1;
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            QuestionTemplate t = pool.get(i);
            QuestionResponse r = new QuestionResponse();
            r.setType(t.isChoice ? 0 : 1);
            r.setTitle(t.title);
            if (t.isChoice && t.options != null) {
                r.setOptions(toJsonArray(t.options));
            }
            r.setAnswer(t.answer);
            r.setScore(10);
            r.setSortOrder(order++);
            result.add(r);
        }
        return result;
    }

    private String getCourseName(Long courseId) {
        try {
            var row = examMapper.findCourseById(courseId);
            return row != null ? row.getName() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractKeyword(String courseName) {
        if (courseName == null) return "";
        String lower = courseName.toLowerCase();
        for (String key : TOPIC_TEMPLATES.keySet()) {
            if (lower.contains(key)) return key;
        }
        if (lower.contains("java") || lower.contains("spring") || lower.contains("boot")) return "spring";
        if (lower.contains("数据")) return "数据库";
        if (lower.contains("vue") || lower.contains("前端")) return "vue";
        if (lower.contains("python") || lower.contains("pandas") || lower.contains("数据分")) return "python";
        if (lower.contains("ai") || lower.contains("人工智能") || lower.contains("机器") || lower.contains("深度")) return "ai";
        if (lower.contains("linux") || lower.contains("操作系统")) return "linux";
        if (lower.contains("结构") || lower.contains("算法")) return "数据结构";
        if (lower.contains("网络") || lower.contains("通信")) return "网络";
        return "";
    }

    private List<QuestionTemplate> findTemplates(String keyword) {
        if (keyword.isEmpty()) return List.of();
        return TOPIC_TEMPLATES.getOrDefault(keyword, List.of());
    }

    private String toJsonArray(String[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(arr[i])).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class QuestionTemplate {
        final boolean isChoice;
        final String title;
        final String[] options;
        final String answer;

        QuestionTemplate(boolean isChoice, String title, String[] options, String answer) {
            this.isChoice = isChoice;
            this.title = title;
            this.options = options;
            this.answer = answer;
        }
    }
}
