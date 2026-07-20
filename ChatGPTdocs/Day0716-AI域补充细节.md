## ＭySQL和向量库一起工作

### 1 MySQL里存了什么

要集成RAG知识库功能，除了要使用Chroma或milvus作为向量数据库来存储向量外，还要在**MySQL数据库里使用表来存储元数据**。到底是哪些元数据？

下面用例子说明：

假设你的 PDF 解析后得到这样一段文字（约 1600 字符）：

【第1段】武汉是中国重要的铁路枢纽城市，位于长江经济带核心位置。

【第2段】武汉站是京广高铁的重要站点，于2009年投入使用。

【第3段】汉口站主要承担沪汉蓉快速通道的列车始发终到业务。

【第4段】武汉东站是武黄城际铁路的重要节点。

【第5段】此外，武汉还规划了武汉西站、天河高铁站等大型铁路枢纽项目。

【第6段】综上所述，武汉目前已建成并投入使用的高铁站主要有武汉站、汉口站和武汉东站三座。

按 500 字符/块、50 字符重叠 切分后，可能得到：

……

MySQL里到底有什么元数据？

#### kb_knowledge_base 表（知识库）

id = 1

name = "高铁百科"

description = "高铁相关知识库"

collection_name = "qiyun_kb_123456789"

status = 1

created_by = 1

created_at = 2026-07-16 09:00:00

updated_at = 2026-07-16 09:30:00

#### kb_document 表（原始文档）

id = 10

kb_id = 1 ← 属于哪个知识库

file_name = "a1b2c3d4.pdf" ← 服务器上存储的文件名

original_name = "武汉高铁枢纽.pdf" ← 上传时的原始文件名

file_size = 364000 ← 文件大小，字节数

file_type = "pdf"

chunk_count = 8 ← 构建后切分成了 8 块

status = 1 ← 1=成功，0=处理中，2=失败

error_msg = null

created_at = 2026-07-16 09:05:00

updated_at = 2026-07-16 09:35:00

注意：MySQL 里**没有存 8 个块的具体内容**，只存了 chunk_count=8 这个数字。

### 2 向量库里存了什么

存的是向量，每个文本块在 Chroma 里对应一条向量记录

### 3 MySQL 和 Chroma 是怎么对应起来的

对应关系只有三个字段：

MySQL kb_knowledge_base.id = Chroma metadata.kb_id

MySQL kb_document.id = Chroma metadata.doc_id

MySQL kb_document.chunk_count = Chroma 中该文档的向量记录条数

**举例1：前端显示“文档列表”**

此时前端要调用 后端的/api/ai/knowledge/1/documents，后端查 MySQL：

SELECT \* FROM kb_document WHERE kb_id = 1;

返回：

\[

{

"id": 10,

"originalName": "武汉高铁枢纽.pdf",

"chunkCount": 8,

"status": 1,

"fileSize": 364000

}

\]

前端就显示：**武汉高铁枢纽.pdf，8 个文本块等字段信息。**

**举例2：回答问题时检索**

用户问“武汉有几座高铁站”，后端到 Chroma 检索，返回最相似的向量记录，比如

vector-7的内容

{

"content": "综上所述，武汉目前已建成并投入使用的高铁站主要有武汉站、汉口站和武汉东站三座。",

"metadata": {

"kb_id": 1,

"doc_id": 10,

"source": "武汉高铁枢纽.pdf",

"chunk_index": 7

}

}

**一句话总结**

MySQL 存的是“全局信息”：知识库有哪些、文档叫什么名字、有多少块、构建成功了没有。

Chroma 存的是“正文”：每个块的具体文字、向量、以及它属于哪个知识库（kb_id）、哪个文档（doc_id）。这两边通过 kb_id 和 doc_id 关联起来。

## 二 执行知识库构建时异常的解决方案

知识库构建时要解析我们上传的文档，并分块，可能会出现timeout或OutOfMemoryError异常，解决方案如下：

1．在 IDEA 中找到 qiyun-ai-service 的启动配置（Run/Debug Configurations），在 **VM options** 里加上：

**\-Dnacos.remote.client.grpc.timeout=10000 -Xmx2g**

2.把 qiyun-ai-service 和 qiyun-gateway 的 Nacos 心跳超时也加大,配在server-addr的下面，如下所示：

server-addr: 127.0.0.1:8848

heart-beat-interval: 5000

heart-beat-timeout: 15000

ip-delete-timeout: 30000

3\. **上传稍微小一点的pdf文件，300K以下的通常不会出现异常。**

## 三 情绪分析

情绪分析是调用Spring AI的ChatClient类的方法，LLM以情绪分析专家的身份对用户发送的内容进行分析得出一个结果，LLM回复一个数字即可。下面的情绪Service实现类中的analyze方法就是情绪分析的具体实现。

package com.whlg.ai.service.impl;  
<br/>import com.whlg.ai.service.SentimentService;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.ai.chat.client.ChatClient;  
import org.springframework.ai.openai.OpenAiChatModel;  
import org.springframework.stereotype.Service;  
<br/>@Service  
public class SentimentServiceImpl implements SentimentService {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(SentimentServiceImpl.class);  
<br/>private final OpenAiChatModel chatModel;  
<br/>public SentimentServiceImpl(OpenAiChatModel chatModel) {  
this.chatModel = chatModel;  
}  
<br/>@Override  
public int analyze(String text) {  
if (text == null || text.isBlank()) {  
return 2;  
}  
try {  
ChatClient chatClient = ChatClient._builder_(chatModel).build();  
String reply = chatClient.prompt()  
.system("你是一个情绪分析专家。请分析下面这句话的客户情绪，只回复一个数字：1表示正面，2表示中性，3表示负面。不要输出任何其他内容。")  
.user(text)  
.call()  
.content();  
<br/>String trimmed = reply == null ? "" : reply.trim();  
_log_.info("情绪分析 | text={} | raw={}", text, trimmed);  
<br/>if (trimmed.contains("1") || trimmed.contains("正面")) {  
return 1;  
} else if (trimmed.contains("3") || trimmed.contains("负面")) {  
return 3;  
}  
return 2;  
} catch (Exception e) {  
_log_.error("情绪分析失败", e);  
return 2;  
}  
}  
}

在COntroler接口中对业务方法进行调用，如下所示：

package com.whlg.ai.controller;  
<br/>import com.whlg.ai.dto.SentimentRequest;  
import com.whlg.ai.service.SentimentService;  
import com.whlg.common.result.Result;  
import io.swagger.v3.oas.annotations.Operation;  
import io.swagger.v3.oas.annotations.tags.Tag;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.web.bind.annotation.PostMapping;  
import org.springframework.web.bind.annotation.RequestBody;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RestController;  
<br/>@Tag(name = "情绪分析", description = "A2 客户情绪分析")  
@RestController  
@RequestMapping("/api/ai/sentiment")  
public class SentimentController {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(SentimentController.class);  
<br/>private final SentimentService sentimentService;  
<br/>public SentimentController(SentimentService sentimentService) {  
this.sentimentService = sentimentService;  
}  
<br/>@Operation(summary = "分析文本情绪", description = "返回 1-正面，2-中性，3-负面")  
@PostMapping("/analyze")  
public Result&lt;Integer&gt; analyze(@RequestBody SentimentRequest request) {  
try {  
if (request == null || request.getText() == null || request.getText().isBlank()) {  
return Result._success_(2);  
}  
int sentiment = sentimentService.analyze(request.getText());  
return Result._success_(sentiment);  
} catch (Exception e) {  
_log_.error("情绪分析接口异常", e);  
return Result._success_(2);  
}  
}  
}

**接口测试：**

**POST** http://localhost:5173/api/ai/sentiment/analyze

Content-Type: application/json

Authorization: Bearer &lt;你的token&gt;

{

"text":"你们的工作让我不太满意"

}

响应：

{

"code": 200,

"message": "success",

"data": 3

}

在前端页面上就显示“负面”

类似的，分析结果为“正面”时，前端显示如下：