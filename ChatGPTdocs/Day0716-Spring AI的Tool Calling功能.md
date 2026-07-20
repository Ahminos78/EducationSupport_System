# Day0716-Spring AI的Tool Calling功能1 Tool Calling概述

## 1.1 什么是Tool Calling

工具调用 (Tool Calling)‌就是允许大语言模型(LLM) 主动调用你提供的工具/方法来获取实时信息、查询数据库或调用外部API等操作的核心能力。模型自己决定什么时候用Tool Calling，用完把结果拿回去生成回答。‌‌‌Spring AI从1.0.0-M6版开始支持**Tool Calling**，在M6版本之前叫Function Calling。

## 1.2 Tool Calling怎么用

Tool Calling 的核心是让模型"突破"自身限制，它自己判断要不要调工具，实际执行还是你的Java代码在干活 。完整流程是：用户提问→模型分析→决定调工具→Spring AI执行你的Java方法→把结果回传给模型→模型生成最终回答 。这样你就能让AI干些实际活儿，比如查实时数据、操作数据库、发邮件这些它原本干不了的事 。‌‌‌

## 1.3 RAG 和 Function Calling 的区别

RAG 用于给模型补充静态知识，让模型能回答超出它本身知识的问题。而Tool Calling能让模型执行工具调用，去查数据库、调用接口等，模型变得更强大。

# 2 Tool Calling快速入门

 场景：给大模型提问今天的日期，默认它答不上来，如下图：

**需求：**改造ai-service模块的代码，加入工具调用功能，让LLM能回答”今天几月几日，星期几？”

步骤：

## 2.1工具类DateTimeTools

在ai-service模块里新建一个工具类，提供三个工具方法，完整示例代码如下：

package com.whlg.ai.tools;  
<br/>import org.springframework.ai.tool.annotation.Tool;  
import org.springframework.ai.tool.annotation.ToolParam;  
import org.springframework.stereotype.Component;  
<br/>import java.time.LocalDate;  
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
import java.time.format.TextStyle;  
import java.util.Locale;  
<br/>_/\*\*  
\* 日期时间工具类  
\* &lt;p&gt;  
\* 供大模型通过 Tool Calling 调用，以获取当前日期、星期几等实时信息。  
\*/  
_@Component  
public class DateTimeTools {  
<br/>_/\*\*  
\* 获取今天的日期  
\*  
\* @param pattern 日期格式，如 yyyy-MM-dd，默认为 yyyy-MM-dd  
\* @return 格式化后的当前日期字符串  
\*/  
_@Tool(name = "getCurrentDate", description = "获取今天的日期，可指定输出格式")  
public String getCurrentDate(  
@ToolParam(description = "日期格式，如 yyyy-MM-dd，默认 yyyy-MM-dd") String pattern) {  
if (pattern == null || pattern.isBlank()) {  
pattern = "yyyy-MM-dd";  
}  
return LocalDate._now_().format(DateTimeFormatter._ofPattern_(pattern));  
}  
<br/>_/\*\*  
\* 获取今天是星期几  
\*  
\* @return 中文星期几，如“星期三”  
\*/  
_@Tool(name = "getCurrentDayOfWeek", description = "获取今天是星期几")  
public String getCurrentDayOfWeek() {  
return LocalDateTime._now_().getDayOfWeek().getDisplayName(TextStyle._FULL_, Locale._CHINA_);  
}  
<br/>_/\*\*  
\* 获取当前日期时间  
\*  
\* @return 当前日期时间字符串，格式 yyyy-MM-dd HH:mm:ss  
\*/  
_@Tool(name = "getCurrentDateTime", description = "获取当前的日期和时间")  
public String getCurrentDateTime() {  
return LocalDateTime._now_().format(DateTimeFormatter._ofPattern_("yyyy-MM-dd HH:mm:ss"));  
}  
}

## 2.2工具调用配置类AiToolConfig

在config包下新建AiToolConfig配置类，把 DateTimeTools 注册为 ChatClient 的默认工具。参考示例如下：

package com.whlg.ai.config;  
<br/>import com.whlg.ai.tools.DateTimeTools;  
import org.springframework.ai.chat.client.ChatClient;  
import org.springframework.ai.openai.OpenAiChatModel;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
<br/>_/\*\*  
\* AI 工具调用配置  
\* &lt;p&gt;  
\* 将 DateTimeTools 注册为 ChatClient 的默认工具，使大模型在需要时能主动获取当前日期和星期几。  
\*/  
_@Configuration  
public class AiToolConfig {  
<br/>@Bean  
public ChatClient chatClient(OpenAiChatModel chatModel, DateTimeTools dateTimeTools) {  
return ChatClient._builder_(chatModel)  
.defaultTools(dateTimeTools)  
.build();  
}  
}

## 2.3 注入已经配置好工具的 ChatClient

改造ChatServiceImpl类，注入已经配置好工具的 ChatClient。具体操作；不再注入chatModel的bean, 统一使用注入进来的ChatClient实例。

完整示例代码如下：

package com.whlg.ai.service.impl;  
<br/>import com.whlg.ai.dto.ChatRequest;  
import com.whlg.ai.dto.DocumentChunk;  
import com.whlg.ai.service.ChatService;  
import com.whlg.ai.service.KnowledgeBaseService;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.ai.chat.client.ChatClient;  
import org.springframework.stereotype.Service;  
import reactor.core.publisher.Flux;  
<br/>import java.util.List;  
<br/>@Service  
public class ChatServiceImpl implements ChatService {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(ChatServiceImpl.class);  
<br/>private final ChatClient chatClient;  
private final KnowledgeBaseService knowledgeBaseService;  
<br/>public ChatServiceImpl(ChatClient chatClient, KnowledgeBaseService knowledgeBaseService) {  
this.chatClient = chatClient;  
this.knowledgeBaseService = knowledgeBaseService;  
}  
<br/>@Override  
public String chat(ChatRequest request) {  
String prompt = buildPrompt(request);  
ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(prompt);  
String systemPrompt = buildSystemPrompt(request);  
if (!systemPrompt.isBlank()) {  
spec.system(systemPrompt);  
}  
String reply = spec.call().content();  
_log_.info("非流式对话完成 | 消息: {} | 回复: {}", request.getMessage(), reply);  
return reply;  
}  
<br/>@Override  
public Flux&lt;String&gt; chatStream(ChatRequest request) {  
String prompt = buildPrompt(request);  
String systemPrompt = buildSystemPrompt(request);  
<br/>ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(prompt);  
if (!systemPrompt.isBlank()) {  
spec.system(systemPrompt);  
}  
<br/>_log_.info("开始流式对话 | 消息: {} | 使用 RAG: {}",  
request.getMessage(), request.getKbId() != null);  
<br/>return spec.stream()  
.content()  
.onErrorResume(error -> {  
_log_.error("流式对话异常", error);  
return Flux._just_("\[ERROR\] 对话失败：" + error.getMessage());  
});  
}  
<br/>private String buildSystemPrompt(ChatRequest request) {  
if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {  
return request.getSystemPrompt();  
}  
if (request.getKbId() != null) {  
return "你是七尾云智能客服助手。请基于下面提供的参考资料回答用户问题。如果参考资料中没有答案，请明确告知用户：\\"根据现有资料，我暂时无法回答该问题。\\"不要编造答案。回答尽量简洁、专业。";  
}  
return "你是七尾云智能客服助手。请用简洁、专业、友好的语言回答用户问题。如果不知道答案，请诚实告知，不要编造。";  
}  
<br/>private String buildPrompt(ChatRequest request) {  
StringBuilder prompt = new StringBuilder();  
if (request.getKbId() != null) {  
List&lt;DocumentChunk&gt; chunks = knowledgeBaseService.searchSimilar(  
request.getKbId(), request.getMessage(), request.getTopK());  
if (!chunks.isEmpty()) {  
prompt.append("参考资料：\\n");  
for (int i = 0; i < chunks.size(); i++) {  
DocumentChunk chunk = chunks.get(i);  
prompt.append("\[").append(i + 1).append("\] ");  
prompt.append(chunk.getContent());  
prompt.append("\\n（来源：").append(chunk.getSourceDocument()).append("）\\n\\n");  
}  
prompt.append("请基于以上参考资料回答问题，不要引用资料之外的常识。\\n\\n");  
}  
}  
prompt.append("用户问题：").append(request.getMessage());  
return prompt.toString();  
}  
}

##  2.4 测试

# 3 Tool Calling实现查询数据库

需求：给大模型提问“列出你们公司前5位客户的姓名和手机号”。

很明显，LLM本身回答不了这个问题的，需要查询数据库。 实现步骤如下：

## 3.1新建CustomerMapper

在ai-service 模块新建CustomerMapper，直接查MySQL 数据库里的 customer 表。

package com.whlg.ai.mapper;  
<br/>import com.baomidou.mybatisplus.core.mapper.BaseMapper;  
import com.whlg.common.entity.Customer;  
import org.apache.ibatis.annotations.Mapper;  
<br/>_/\*\*  
\* 客户 Mapper 接口  
\* &lt;p&gt;  
\* 供 AI 服务本地查询客户数据使用。  
\*/  
<br/>_public interface CustomerMapper extends BaseMapper&lt;Customer&gt; {  
}

## 3.2 工具类CustomerQueryTools

新建工具类CustomerQueryTools，定义了queryTop5Customers 工具方法，按 id 升序取前 5 条客户，返回 ID、姓名、手机号。

package com.whlg.ai.tools;  
<br/>import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  
import com.whlg.ai.mapper.CustomerMapper;  
import com.whlg.common.entity.Customer;  
import org.springframework.ai.tool.annotation.Tool;  
import org.springframework.stereotype.Component;  
<br/>import java.util.List;  
import java.util.stream.Collectors;  
<br/>_/\*\*  
\* 客户信息查询工具类  
\* &lt;p&gt;  
\* 供大模型通过 Tool Calling 调用，以查询企业客户数据。  
\*/  
_@Component  
public class CustomerQueryTools {  
<br/>private final CustomerMapper customerMapper;  
<br/>public CustomerQueryTools(CustomerMapper customerMapper) {  
this.customerMapper = customerMapper;  
}  
<br/>_/\*\*  
\* 查询前 5 个客户（按 id 升序），返回姓名和手机号  
\*  
\* @return 格式化后的客户列表字符串  
\*/  
_@Tool(name = "queryTop5Customers", description = "查询公司前5个客户的信息，按id升序返回姓名和手机号")  
public String queryTop5Customers() {  
LambdaQueryWrapper&lt;Customer&gt; wrapper = new LambdaQueryWrapper<>();  
wrapper.orderByAsc(Customer::getId).last("limit 5");  
List&lt;Customer&gt; list = customerMapper.selectList(wrapper);  
<br/>if (list == null || list.isEmpty()) {  
return "当前没有客户数据";  
}  
return list.stream()  
.map(c -> "ID：" + c.getId() + "，姓名：" + c.getName() + "，手机号：" + c.getPhone())  
.collect(Collectors._joining_("\\n"));  
}  
}

## 3.3 AiToolConfig配置类

  
修改AiToolConfig，把 CustomerQueryTools 和 DateTimeTools 一起注册为默认工具。

关键：defaultTools(dateTimeTools,customerQueryTools)

## 3.4 测试

重启应用测试即可