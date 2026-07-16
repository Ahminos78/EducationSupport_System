# 基于Chroma的RAG知识库构建实战指南1 前言

本实战指南文档是以“知识库构建完全指南-从文档解析到Milvus向量存储.md”为基础编写出来的，重点阐述MD文档中的“方案二：Docker + Chroma（更简单，数据可持久化）”，

因为有些同学的电脑上安装了Docker Desktop后可能拉取不到Milvus相关的三个镜像，这样就没办法实现向量存储了。所以，本文着重描述用Chroma代替Milvus以实现向量存储的一些细节。**默认阅读此文的读者已安装好了适合自己操作系统的Docker Desktop(前提)。**

# 2 Docker镜像加速器的配置

Docker默认去docker.hub上下载(拉取)镜像，很多时候是拉取不下来的，容易出现超时异常(timeout)，这时我们应该为Docker配置镜像加速器，步骤如下：

1\. 打开 Docker Desktop

2\. 点击右上角的 Settings（设置）图标

3\. 在左侧菜单中选择 Docker Engine

4\. 在右侧配置文件中添加 registry-mirrors 字段：

"registry-mirrors": \[

"http://hub-mirror.c.163.com",

"https://docker.m.daocloud.io",

"https://docker.mirrors.ustc.edu.cn"

\]

5\. **点击 Apply & Restart（应用并重启）**

6\. 等待 Docker 重启完成

配置完成后，测试是否生效的方法：在命令行窗口输入docker info

在输出中查找 Registry Mirrors，能看到你配置的镜像地址就说明成功了.

# 3 使用chroma离线镜像包启动容器

## 3.1 拉取镜像并保存到本地

1\. 先执行拉取命令：docker pull chromadb/chroma:0.5.20

会使用前面配置的加速器去下载镜像，如果有超时异常，那就加上前缀(加速器地址)，如下：

docker pull docker.m.daocloud.io/chromadb/chroma:0.5.20

docker tag docker.m.daocloud.io/chromadb/chroma:0.5.20 chromadb/chroma:0.5.20

这里多了一步tag打标签操作，目的是让Docker能够识别到我们的镜像。

2.保存镜像

提前在D盘新建一个目录，如Docker-images，然后cmd命令行窗口进入该目录，执行

docker save chromadb/chroma:0.5.20 -o chroma-0.5.20.tar

这样镜像文件就变成了一个tar文件，并保存(输出)到当前目录 D:\\Docker-images，以后换电脑或重装系统了可以直接使用这个离线tar包，不用重新拉取镜像了。

## 3.2 启动chroma容器

提前创建一个D:/chroma-data目录，按下面的步骤来使用镜像去创建容器。

1）加载离线包，要进入到tar包所在目录执行如下命令

docker load -i chroma-0.5.20.tar

2）启动容器

docker run -d --name chroma -p 8000:8000 -v D:\\chroma-data:/chroma/chroma -e ANONYMIZED_TELEMETRY=False chromadb/chroma:0.5.20

执行完该 命令后，返回容器的ID，在Doker Desktop中能看到

至此，Chroma向量数据库就部署起来了，接下来就要改造程序来进行向量数据的存储了。

# 4 在qiyun-ai-demo中整合向量存储功能

## 4.1 改造qiyun-ai-demo的pom.xml

为了能在qiyun-ai-demo这个演示案例中集成向量存储功能，考虑到保证spring-ai与Chroma的兼容性，需要修改原来的父工程smart-backend中pom.xml，把spring AI的版本降低，如下：&lt;spring-ai.version&gt;1.0.0-M6&lt;/spring-ai.version&gt;

然后在本模块的pom.xml中添加如下依赖：

_&lt;!-- Spring AI OpenAI Starter（M6 旧命名方式，通过 OpenAI 兼容协议接入 DeepSeek） --&gt;  
_&lt;dependency&gt;  
&lt;groupId&gt;org.springframework.ai&lt;/groupId&gt;  
&lt;artifactId&gt;spring-ai-openai-spring-boot-starter&lt;/artifactId&gt;  
&lt;/dependency&gt;

_&lt;!-- Spring AI Chroma 向量存储（M6 命名方式） --&gt;  
_&lt;dependency&gt;  
&lt;groupId&gt;org.springframework.ai&lt;/groupId&gt;  
&lt;artifactId&gt;spring-ai-chroma-store-spring-boot-starter&lt;/artifactId&gt;  
&lt;/dependency&gt;  
<br/>_&lt;!-- Apache PDFBox：解析 PDF 文档 --&gt;  
_&lt;dependency&gt;  
&lt;groupId&gt;org.apache.pdfbox&lt;/groupId&gt;  
&lt;artifactId&gt;pdfbox&lt;/artifactId&gt;  
&lt;version&gt;2.0.32&lt;/version&gt;  
&lt;/dependency&gt;  
<br/>_&lt;!-- Apache POI：解析 Word 文档（docx） --&gt;  
_&lt;dependency&gt;  
&lt;groupId&gt;org.apache.poi&lt;/groupId&gt;  
&lt;artifactId&gt;poi-ooxml&lt;/artifactId&gt;  
&lt;version&gt;5.2.5&lt;/version&gt;  
&lt;/dependency&gt;

另外，如果后期项目开发完毕，需要打成jar包独立部署，还可以添加打包插件

&lt;build&gt;  
&lt;plugins&gt;  
&lt;plugin&gt;  
&lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;  
&lt;artifactId&gt;spring-boot-maven-plugin&lt;/artifactId&gt;  
&lt;configuration&gt;  
&lt;excludes&gt;  
&lt;exclude&gt;  
&lt;groupId&gt;org.projectlombok&lt;/groupId&gt;  
&lt;artifactId&gt;lombok&lt;/artifactId&gt;  
&lt;/exclude&gt;  
&lt;/excludes&gt;  
&lt;/configuration&gt;  
&lt;/plugin&gt;  
&lt;/plugins&gt;  
&lt;/build&gt;

## 4.2 改造qiyun-ai-demo的yml文件

server:  
port: 8083  
<br/>spring:  
application:  
name: qiyun-ai-demo  
ai:  
openai:  
api-key: {你的deepSeek开放平台的key}  
base-url: https://api.deepseek.com  
chat:  
api-key: sk-64d09c3a6fde411eb4bed249db715f74  
options:  
model: deepseek-v4-pro  
vectorstore:  
chroma:  
host: http://localhost:8000  
collection-name: qiyun_knowledge_base  
initialize-schema: true  
<br/>_\# SiliconFlow 嵌入模型配置（DeepSeek 不支持 embeddings，用硅基流动替代）  
_siliconflow:  
api-key: {你的硅基流动开放平台的key}  
base-url: https://api.siliconflow.cn  
embedding:  
model: BAAI/bge-large-zh-v1.5  
<br/>logging:  
level:  
org.springframework.ai: INFO  
com.whlg.ai.demo: DEBUG

DeepSeek没有支持向量存储的模型，因此还需要去申请硅基流动的Key.

1.  登录：https://siliconflow.cn/ ，用手机号+验证码登录，然后进行实名认证

然后在模型广场里能看到嵌入模型BAAI/bge-large-zh-v1.5

1.  创建API Key

## 4.3 开发知识库接口

知识库的构建分为四步：

**文档解析**：把 PDF、Word、TXT 等格式的文档提取成纯文本

**文本分块**：把长文本切成合适大小的块（每块约 500 字符，带重叠）

**向量化**：调用 Embedding 模型，把每个文本块变成向量

**存储**：把向量和原文一起存入向量数据库

按照以上流程，开发对应的接口。为了方便演示，提前在resources目录下存放了txt文件，方便对这些文件进行解析，分块，向量化及存储操作。

### 4.3.1 DeepSeekAi配置类

package com.whlg.ai.demo.config;  
<br/>import org.springframework.ai.document.MetadataMode;  
import org.springframework.ai.openai.OpenAiEmbeddingModel;  
import org.springframework.ai.openai.api.OpenAiApi;  
import org.springframework.ai.retry.RetryUtils;  
import org.springframework.beans.factory.annotation.Value;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.Primary;  
import org.springframework.http.MediaType;  
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;  
import org.springframework.web.client.RestClient;  
import org.springframework.web.reactive.function.client.WebClient;  
<br/>import java.util.ArrayList;  
import java.util.List;  
<br/>_/\*\*  
\* 嵌入模型配置类  
\* 用 SiliconFlow 的 BAAI/bge-large-zh-v1.5 模型做向量化，  
\* 解决 DeepSeek 不支持 embeddings 接口的问题。  
\*  
\* 分工：  
\* - DeepSeek → 负责 Chat（大模型对话、情绪分析等）  
\* - SiliconFlow → 负责 Embedding（文本向量化，供 RAG 检索用）  
\* - Chroma → 负责向量存储  
\*/  
_@Configuration  
public class DeepSeekAiConfig {  
<br/>@Value("${siliconflow.base-url}")  
private String siliconflowBaseUrl;  
<br/>@Value("${siliconflow.api-key}")  
private String siliconflowApiKey;  
<br/>@Value("${siliconflow.embedding.model}")  
private String embeddingModel;  
<br/>@Bean  
@Primary  
public OpenAiEmbeddingModel siliconFlowEmbeddingModel() {  
_// 自定义 Jackson 消息转换器，支持各种 Content-Type  
_MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();  
List&lt;MediaType&gt; mediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());  
mediaTypes.add(MediaType._parseMediaType_("application/octet-stream"));  
jsonConverter.setSupportedMediaTypes(mediaTypes);  
<br/>_// 构建 RestClient，指向 SiliconFlow API  
_RestClient.Builder restClientBuilder = RestClient._builder_()  
.baseUrl(siliconflowBaseUrl)  
.messageConverters(converters -> {  
converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);  
converters.add(jsonConverter);  
});  
<br/>_// 创建 OpenAiApi（SiliconFlow 兼容 OpenAI 格式）  
_OpenAiApi openAiApi = new OpenAiApi(  
siliconflowBaseUrl,  
siliconflowApiKey,  
"/v1/chat/completions",  
"/v1/embeddings",  
restClientBuilder,  
WebClient._builder_(),  
RetryUtils._DEFAULT_RESPONSE_ERROR_HANDLER  
_);  
<br/>_// 创建 EmbeddingModel  
_return new OpenAiEmbeddingModel(  
openAiApi,  
MetadataMode._EMBED_,  
org.springframework.ai.openai.OpenAiEmbeddingOptions._builder_()  
.model(embeddingModel)  
.build()  
);  
}  
}

### 4.3.2 DocumentChunk类

DocumentChunk类用于封装文本分块的请求参数

package com.whlg.ai.demo.dto;  
<br/>import java.util.Map;  
<br/>_/\*\*  
\* 文本块（Chunk）数据传输对象  
\* 知识库构建的核心数据结构，表示一篇文档被切分后的一个文本块。  
\* 每个文本块会被向量化后存入 Chroma 向量数据库。  
\*  
\* 为什么需要 Chunk？  
\* 大模型的上下文窗口（Context Window）是有限的，一篇几十页的企业文档  
\* 不可能全部塞进 Prompt 里。所以需要把长文档切分成多个小块，  
\* 检索时只返回最相关的几个块给大模型参考。  
\*  
\* 字段说明  
\* - id：文档块的唯一标识，用于向量库中定位和删除  
\* - content：文本块的实际内容，会被拿去做向量化  
\* - metadata：元数据（如文件名、页码、块序号等），方便检索后溯源  
\* - chunkIndex：该块在原文档中的序号（第几个块）  
\* - sourceDocument：来源文档名称，用于标识这是哪篇文档的内容  
\*/  
_public class DocumentChunk {  
<br/>_// 文档块的唯一 ID（由系统生成，用于向量库存储）  
_private String id;  
<br/>_// 文本块的内容（核心字段，会被 Embedding 模型向量化）  
_private String content;  
<br/>_// 元数据 Map，存储额外信息：  
// - source: 来源文件名  
// - chunk_index: 块序号  
// - page_number: 页码（PDF 文档有）  
// - category: 文档分类  
// - ... 其他自定义字段  
_private Map&lt;String, Object&gt; metadata;  
<br/>_// 该块在原文档中的索引（第几个块，从 0 开始）  
_private int chunkIndex;  
<br/>_// 来源文档名称（如 "公司介绍.pdf"）  
_private String sourceDocument;  
<br/>_// 无参构造函数（Jackson 序列化需要）  
_public DocumentChunk() {  
}  
<br/>_// 便捷构造函数  
_public DocumentChunk(String content, int chunkIndex, String sourceDocument) {  
this.content = content;  
this.chunkIndex = chunkIndex;  
this.sourceDocument = sourceDocument;  
}  
<br/>_// ========== Getter & Setter ==========  
<br/>_public String getId() {  
return id;  
}  
<br/>public void setId(String id) {  
this.id = id;  
}  
<br/>public String getContent() {  
return content;  
}  
<br/>public void setContent(String content) {  
this.content = content;  
}  
<br/>public Map&lt;String, Object&gt; getMetadata() {  
return metadata;  
}  
<br/>public void setMetadata(Map&lt;String, Object&gt; metadata) {  
this.metadata = metadata;  
}  
<br/>public int getChunkIndex() {  
return chunkIndex;  
}  
<br/>public void setChunkIndex(int chunkIndex) {  
this.chunkIndex = chunkIndex;  
}  
<br/>public String getSourceDocument() {  
return sourceDocument;  
}  
<br/>public void setSourceDocument(String sourceDocument) {  
this.sourceDocument = sourceDocument;  
}  
<br/>@Override  
public String toString() {  
return "DocumentChunk{" +  
"id='" + id + '\\'' +  
", content='" + (content != null ? content.substring(0, Math._min_(50, content.length())) + "..." : "null") + '\\'' +  
", chunkIndex=" + chunkIndex +  
", sourceDocument='" + sourceDocument + '\\'' +  
'}';  
}  
}

### 4.3.3 KnowledgeBaseBuildRequest类

KnowledgeBaseBuildRequest类用于封装知识库构建请求的参数，配置文件里配置了集合名

collection-name: qiyun_knowledge_base，在Spring AI启动时读取 yml文件 中的配置，创建 ChromaVectorStore Bean，这个 Bean 内部就绑定了 qiyun_knowledge_base 这个集合名。之后所有的写入和检索操作，都自动指向该集合。

package com.whlg.ai.demo.dto;  
<br/>_/\*\*  
\* 知识库构建请求 DTO  
\* 前端触发知识库构建时发送的请求参数。  
\*  
\* 参数说明  
\* - documentPath：要处理的文档路径（可以是单个文件或目录）  
\* - chunkSize：每个文本块的大小（字符数），默认 500  
\* - chunkOverlap：相邻块之间的重叠字符数，默认 50  
\* - collectionName：Chroma集合名称，默认使用配置文件中的名称  
\*  
\* 什么是 Chunk Overlap？  
\* 想象一下把一篇文章切成几段，如果切得太干脆，  
\* 可能会把一个完整的句子或段落切成两半，导致语义断裂。  
\* 重叠（Overlap）就是让相邻的两个块共享一部分内容，  
\* 这样即使切分点正好在句子中间，其中一个块也能包含完整的句子。  
\*/  
_public class KnowledgeBaseBuildRequest {  
<br/>_// 文档路径（可以是单个文件路径，也可以是目录路径）  
// 例如："D:/docs/company_intro.pdf" 或 "D:/docs/"  
_private String documentPath;  
<br/>_// 每个文本块的大小（字符数）  
// 建议值：  
// - 通用场景：500 - 1000 字符  
// - 详细问答：300 - 500 字符（更精准）  
// - 摘要类场景：1000 - 2000 字符（更多上下文）  
_private int chunkSize = 500;  
<br/>_// 相邻块之间的重叠字符数  
// 一般设为 chunkSize 的 10% - 20% 比较合适。  
// 重叠太多会增加冗余，太少又起不到保持上下文的作用。  
_private int chunkOverlap = 50;  
<br/>_// Chroma集合名称（可选，不填则使用默认配置）  
_private String collectionName;  
<br/>_// 无参构造函数  
_public KnowledgeBaseBuildRequest() {  
}  
<br/>_// 便捷构造函数  
_public KnowledgeBaseBuildRequest(String documentPath, int chunkSize, int chunkOverlap) {  
this.documentPath = documentPath;  
this.chunkSize = chunkSize;  
this.chunkOverlap = chunkOverlap;  
}  
<br/>_// ========== Getter & Setter ==========  
<br/>_public String getDocumentPath() {  
return documentPath;  
}  
<br/>public void setDocumentPath(String documentPath) {  
this.documentPath = documentPath;  
}  
<br/>public int getChunkSize() {  
return chunkSize;  
}  
<br/>public void setChunkSize(int chunkSize) {  
this.chunkSize = chunkSize;  
}  
<br/>public int getChunkOverlap() {  
return chunkOverlap;  
}  
<br/>public void setChunkOverlap(int chunkOverlap) {  
this.chunkOverlap = chunkOverlap;  
}  
<br/>public String getCollectionName() {  
return collectionName;  
}  
<br/>public void setCollectionName(String collectionName) {  
this.collectionName = collectionName;  
}  
}

### 4.3.4文档解析服务类DocumentParserService

package com.whlg.ai.demo.service;  
<br/>import org.apache.pdfbox.pdmodel.PDDocument;  
import org.apache.pdfbox.text.PDFTextStripper;  
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;  
import org.apache.poi.xwpf.usermodel.XWPFDocument;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.stereotype.Service;  
<br/>import java.io.File;  
import java.io.FileInputStream;  
import java.io.IOException;  
import java.nio.charset.StandardCharsets;  
import java.nio.file.Files;  
import java.nio.file.Path;  
import java.nio.file.Paths;  
import java.util.ArrayList;  
import java.util.List;  
<br/>_/\*\*  
\* 文档解析服务  
\* 负责将各种格式的企业文档解析为纯文本，为后续的分块和向量化做准备。  
\*  
\* 支持的文档格式  
\* - .txt：纯文本文件（最简单，直接读取）  
\* - .pdf：PDF 文档（使用 Apache PDFBox 解析）  
\* - .docx：Word 文档（使用 Apache POI 解析）  
\*  
\* 什么是文档解析？  
\* 企业里的文档格式五花八门（PDF、Word、PPT、Excel...），每种格式都有自己的二进制结构，  
\* 不能直接拿来做向量化。文档解析的工作就是把这些不同格式的文档中的文字内容提取出来，  
\* 变成统一的纯文本字符串。  
\* 就像你要做沙拉，得先把不同包装（PDF/纸盒/塑料袋拆掉，拿出里面的食材（文字）。  
\*/  
_@Service  
public class DocumentParserService {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(DocumentParserService.class);  
<br/>_/\*\*  
\* 解析单个文档  
\* &lt;p&gt;  
\* 根据文件扩展名自动选择合适的解析器，提取纯文本内容。  
\* &lt;/p&gt;  
\*  
\* @param filePath 文档的绝对路径  
\* @return 解析后的纯文本内容  
\* @throws IOException 文件读取或解析失败时抛出  
\*/  
_public String parseDocument(String filePath) throws IOException {  
File file = new File(filePath);  
<br/>_// ========== 第一步：检查文件是否存在 ==========  
_if (!file.exists()) {  
throw new IOException("文件不存在: " + filePath);  
}  
if (!file.isFile()) {  
throw new IOException("路径不是一个文件: " + filePath);  
}  
<br/>String fileName = file.getName().toLowerCase();  
_log_.info("开始解析文档: {}", fileName);  
<br/>_// ========== 第二步：根据文件扩展名选择解析方式 ==========  
_String content;  
if (fileName.endsWith(".txt")) {  
content = parseTxt(file);  
} else if (fileName.endsWith(".pdf")) {  
content = parsePdf(file);  
} else if (fileName.endsWith(".docx")) {  
content = parseDocx(file);  
} else {  
_// 不支持的格式，抛出异常  
_throw new IllegalArgumentException("不支持的文档格式: " + fileName +  
"，目前支持 .txt、.pdf、.docx");  
}  
<br/>_log_.info("文档解析完成 | 文件名: {} | 字符数: {}", fileName, content.length());  
return content;  
}  
<br/>_/\*\*  
\* 批量解析目录下的所有文档  
\* &lt;p&gt;  
\* 遍历指定目录，解析所有支持格式的文件，返回文件名和内容的映射。  
\* &lt;/p&gt;  
\*  
\* @param dirPath 目录路径  
\* @return 解析结果列表，每项包含文件名和内容  
\* @throws IOException 目录读取失败时抛出  
\*/  
_public List&lt;ParsedDocument&gt; parseDirectory(String dirPath) throws IOException {  
Path dir = Paths._get_(dirPath);  
<br/>if (!Files._exists_(dir)) {  
throw new IOException("目录不存在: " + dirPath);  
}  
if (!Files._isDirectory_(dir)) {  
throw new IOException("路径不是目录: " + dirPath);  
}  
<br/>List&lt;ParsedDocument&gt; results = new ArrayList<>();  
<br/>_// 遍历目录下的所有文件  
_try (var stream = Files._list_(dir)) {  
stream.filter(Files::_isRegularFile_).forEach(path -> {  
try {  
String fileName = path.getFileName().toString();  
_// 只处理支持的格式  
_if (isSupportedFormat(fileName)) {  
String content = parseDocument(path.toString());  
results.add(new ParsedDocument(fileName, content));  
_log_.info("已解析: {} ({} 字符)", fileName, content.length());  
}  
} catch (Exception e) {  
_log_.warn("解析文件失败: {} | 原因: {}", path.getFileName(), e.getMessage());  
}  
});  
}  
<br/>_log_.info("目录解析完成 | 共解析 {} 个文件", results.size());  
return results;  
}  
<br/>_/\*\*  
\* 判断文件是否为支持的格式  
\*/  
_private boolean isSupportedFormat(String fileName) {  
String lower = fileName.toLowerCase();  
return lower.endsWith(".txt") || lower.endsWith(".pdf") || lower.endsWith(".docx");  
}  
<br/>_/\*\*  
\* 解析 TXT 纯文本文件  
\* &lt;p&gt;  
\* 最简单的格式，直接读取文件内容即可。  
\* 使用 UTF-8 编码读取，避免中文乱码。  
\* &lt;/p&gt;  
\*/  
_private String parseTxt(File file) throws IOException {  
_// Files.readString 是 Java 11+ 提供的便捷方法，  
// 一次性读取整个文件为字符串  
_return Files._readString_(file.toPath(), StandardCharsets._UTF_8_);  
}  
<br/>_/\*\*  
\* 解析 PDF 文档  
\* 使用 Apache PDFBox 库提取 PDF 中的文本。  
\*  
\* PDFBox 工作原理  
\* 1. 加载 PDF 文件到内存（PDDocument）  
\* 2. 创建文本提取器（PDFTextStripper）  
\* 3. 逐页提取文本内容  
\* 4. 关闭资源  
\*  
\* 注意事项  
\* - 扫描版 PDF（图片型 PDF）无法提取文字，需要 OCR  
\* - 复杂排版的 PDF 可能提取顺序不对  
\* - 加密的 PDF 需要密码才能打开  
\*/  
_private String parsePdf(File file) throws IOException {  
_// try-with-resources 语法：自动关闭资源，无需手动写 finally 关闭  
_try (PDDocument document = PDDocument._load_(file)) {  
_// 创建文本提取器  
_PDFTextStripper stripper = new PDFTextStripper();  
<br/>_// 设置排序：按照阅读顺序提取文本（对于多栏排版的 PDF 很重要）  
_stripper.setSortByPosition(true);  
<br/>_// 提取所有页面的文本  
// 也可以通过 setStartPage()/setEndPage() 指定页码范围  
_String text = stripper.getText(document);  
<br/>_// 去除多余的空行和空白字符  
_return cleanText(text);  
}  
}  
<br/>_/\*\*  
\* 解析 Word 文档（.docx）  
\* 使用 Apache POI 库提取 Word 文档中的文本。  
\* 注意：只支持 .docx 格式（Office 2007+），不支持旧版 .doc 格式。  
\*/  
_private String parseDocx(File file) throws IOException {  
try (FileInputStream fis = new FileInputStream(file);  
XWPFDocument document = new XWPFDocument(fis);  
XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {  
<br/>_// 提取全部文本内容  
_String text = extractor.getText();  
return cleanText(text);  
}  
}  
<br/>_/\*\*  
\* 清理文本  
\* 去除多余的空白字符、空行，规范化文本格式。  
\* 这一步很重要，因为 PDF 和 Word 提取的文本经常有大量无用的空格和换行。  
\*/  
_private String cleanText(String text) {  
if (text == null || text.isEmpty()) {  
return "";  
}  
<br/>_// 1. 把 Windows 换行(\\r\\n) 和旧 Mac 换行(\\r) 统一为 Unix 换行(\\n)  
_text = text.replace("\\r\\n", "\\n").replace("\\r", "\\n");  
<br/>_// 2. 把连续多个空行压缩成一个空行  
// (3个以上换行变成2个换行)  
_text = text.replaceAll("\\\\n{3,}", "\\n\\n");  
<br/>_// 3. 把行首行尾的空格去掉（每行）  
// 先按行分割，去掉每行首尾空白，再拼回去  
_StringBuilder result = new StringBuilder();  
String\[\] lines = text.split("\\\\n");  
for (String line : lines) {  
String trimmed = line.trim();  
if (!trimmed.isEmpty()) {  
result.append(trimmed).append("\\n");  
}  
}  
<br/>_// 4. 去掉首尾多余的空白  
_return result.toString().trim();  
}  
<br/>_/\*\*  
\* 解析结果内部类  
\* 封装文件名和对应的文本内容。  
\* 用 record 是 Java 16+ 的新特性，自动生成构造函数、getter、equals、hashCode、toString。  
\*/  
_public record ParsedDocument(String fileName, String content) {}  
}

### 4.3.5 文本分块服务类TextSplitterService

package com.whlg.ai.demo.service;  
<br/>import com.whlg.ai.demo.dto.DocumentChunk;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.stereotype.Service;  
<br/>import java.util.ArrayList;  
import java.util.List;  
<br/>_/\*\*  
\* 文本分块服务（Text Splitter / Chunking）  
\* 负责将长文本切分成多个合适大小的文本块（Chunk），  
\* 是 RAG（检索增强生成）系统中的关键步骤之一。  
\*  
\* 为什么要做文本分块？  
\* 1. 上下文窗口限制：大模型能处理的 Token 数量有限，不能把整篇长文档都塞进去  
\* 2. 检索精度：小块的语义更集中，向量检索时更容易找到最相关的内容  
\* 3. 性能效率：小块向量化更快，存储和检索也更高效  
\*  
\* 分块策略  
\* 本服务实现的是最经典的固定大小分块 + 重叠策略：  
\* - 按字符数切分，简单高效  
\* - 支持重叠（Overlap），避免语义在切分点断裂  
\* - 优先在段落、句子边界处切分，尽量保持语义完整  
\*/  
_@Service  
public class TextSplitterService {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(TextSplitterService.class);  
<br/>_/\*\*  
\* 默认分块大小（字符数）  
\*/  
_public static final int _DEFAULT_CHUNK_SIZE_ \= 500;  
<br/>_/\*\*  
\* 默认重叠大小（字符数）  
\*/  
_public static final int _DEFAULT_CHUNK_OVERLAP_ \= 50;  
<br/>_/\*\*  
\* 将文本切分为多个块  
\* 这是最核心的方法。使用"固定大小 + 重叠"的策略，  
\* 并且尽量在自然的边界（段落、句子）处切分。  
\*  
\* 切分流程  
\* 1. 先按段落分割（两个换行符 \\n\\n）  
\* 2. 逐个段落累积，当累积长度接近 chunkSize 时切一刀  
\* 3. 每个块包含 overlap 个字符的重叠内容  
\* 4. 如果单个段落就超过 chunkSize，再按句子细分  
\*  
\* @param text 要切分的完整文本  
\* @param chunkSize 每个块的目标大小（字符数）  
\* @param chunkOverlap 相邻块的重叠大小（字符数）  
\* @param sourceDocument 来源文档名（用于元数据）  
\* @return 切分后的文档块列表  
\*/  
_public List&lt;DocumentChunk&gt; splitText(String text, int chunkSize, int chunkOverlap, String sourceDocument) {  
_// ========== 参数校验 ==========  
_if (text == null || text.isEmpty()) {  
return new ArrayList<>();  
}  
if (chunkSize <= 0) {  
chunkSize = _DEFAULT_CHUNK_SIZE_;  
}  
if (chunkOverlap < 0) {  
chunkOverlap = _DEFAULT_CHUNK_OVERLAP_;  
}  
_// 重叠不能大于块大小  
_if (chunkOverlap >= chunkSize) {  
chunkOverlap = chunkSize / 10;  
}  
<br/>_log_.debug("开始切分文本 | 来源: {} | 总字符数: {} | 块大小: {} | 重叠: {}",  
sourceDocument, text.length(), chunkSize, chunkOverlap);  
<br/>List&lt;DocumentChunk&gt; chunks = new ArrayList<>();  
<br/>_// ========== 第一步：先按段落分割 ==========  
// 段落之间用两个或以上换行符分隔  
_String\[\] paragraphs = text.split("\\\\n\\\\s\*\\\\n");  
<br/>StringBuilder currentChunk = new StringBuilder();  
int chunkIndex = 0;  
<br/>for (String paragraph : paragraphs) {  
paragraph = paragraph.trim();  
if (paragraph.isEmpty()) {  
continue;  
}  
<br/>_// ========== 第二步：判断当前块加上这个段落会不会超限 ==========  
// 如果加上这个段落还没超限，就继续累积  
_if (currentChunk.length() + paragraph.length() + 2 <= chunkSize) {  
_// +2 是因为段落之间加两个换行（恢复段落结构）  
_if (currentChunk.length() > 0) {  
currentChunk.append("\\n\\n");  
}  
currentChunk.append(paragraph);  
} else {  
_// ========== 第三步：超限了，先把当前块存起来 ==========  
_if (currentChunk.length() > 0) {  
DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);  
chunks.add(chunk);  
chunkIndex++;  
<br/>_// ========== 第四步：计算重叠部分，作为下一个块的开头 ==========  
_String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);  
currentChunk = new StringBuilder(overlapText);  
}  
<br/>_// ========== 第五步：处理超长段落 ==========  
// 如果单个段落就超过了块大小，需要把这个段落再细分  
_if (paragraph.length() > chunkSize) {  
List&lt;DocumentChunk&gt; subChunks = splitLongParagraph(  
paragraph, chunkSize, chunkOverlap, sourceDocument, chunkIndex);  
chunks.addAll(subChunks);  
chunkIndex += subChunks.size();  
<br/>_// 最后一个子块的重叠部分作为下一块的开头  
_if (!subChunks.isEmpty()) {  
String lastContent = subChunks.get(subChunks.size() - 1).getContent();  
String overlapText = getOverlapText(lastContent, chunkOverlap);  
currentChunk = new StringBuilder(overlapText);  
} else {  
currentChunk = new StringBuilder();  
}  
} else {  
_// 段落不超长，直接加进去  
_if (currentChunk.length() > 0) {  
currentChunk.append("\\n\\n");  
}  
currentChunk.append(paragraph);  
}  
}  
}  
<br/>_// ========== 第六步：处理最后剩下的内容 ==========  
_if (currentChunk.length() > 0) {  
DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);  
chunks.add(chunk);  
}  
<br/>_log_.info("文本切分完成 | 来源: {} | 总块数: {}", sourceDocument, chunks.size());  
return chunks;  
}  
<br/>_/\*\*  
\* 使用默认参数切分文本  
\*/  
_public List&lt;DocumentChunk&gt; splitText(String text, String sourceDocument) {  
return splitText(text, _DEFAULT_CHUNK_SIZE_, _DEFAULT_CHUNK_OVERLAP_, sourceDocument);  
}  
<br/>_/\*\*  
\* 切分超长段落  
\* 当单个段落就超过 chunkSize 时，需要更细粒度的切分。  
\* 先按句子切，如果句子还太长，就按字符硬切。  
\*/  
_private List&lt;DocumentChunk&gt; splitLongParagraph(String paragraph, int chunkSize, int chunkOverlap,  
String sourceDocument, int startIndex) {  
List&lt;DocumentChunk&gt; chunks = new ArrayList<>();  
<br/>_// 先按句子分割（中文用句号、问号、感叹号，英文用 . ? !）  
_String\[\] sentences = paragraph.split("(?<=\[。！？.!?\])");  
<br/>StringBuilder currentChunk = new StringBuilder();  
int chunkIndex = startIndex;  
<br/>for (String sentence : sentences) {  
sentence = sentence.trim();  
if (sentence.isEmpty()) {  
continue;  
}  
<br/>if (currentChunk.length() + sentence.length() <= chunkSize) {  
currentChunk.append(sentence);  
} else {  
_// 保存当前块  
_if (currentChunk.length() > 0) {  
DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);  
chunks.add(chunk);  
chunkIndex++;  
<br/>_// 计算重叠  
_String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);  
currentChunk = new StringBuilder(overlapText);  
}  
<br/>_// 如果单个句子都比块大，只能按字符硬切了  
_if (sentence.length() > chunkSize) {  
List&lt;DocumentChunk&gt; hardChunks = splitHard(sentence, chunkSize, chunkOverlap,  
sourceDocument, chunkIndex);  
chunks.addAll(hardChunks);  
chunkIndex += hardChunks.size();  
<br/>if (!hardChunks.isEmpty()) {  
String lastContent = hardChunks.get(hardChunks.size() - 1).getContent();  
String overlapText = getOverlapText(lastContent, chunkOverlap);  
currentChunk = new StringBuilder(overlapText);  
} else {  
currentChunk = new StringBuilder();  
}  
} else {  
currentChunk.append(sentence);  
}  
}  
}  
<br/>_// 处理最后一块  
_if (currentChunk.length() > 0) {  
DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);  
chunks.add(chunk);  
}  
<br/>return chunks;  
}  
<br/>_/\*\*  
\* 按字符硬切分（最后手段）  
\* 当句子都超长时，只能按字符数硬切。  
\* 这种情况比较少见，但对于无标点的长文本需要处理。  
\*/  
_private List&lt;DocumentChunk&gt; splitHard(String text, int chunkSize, int chunkOverlap,  
String sourceDocument, int startIndex) {  
List&lt;DocumentChunk&gt; chunks = new ArrayList<>();  
int position = 0;  
int chunkIndex = startIndex;  
<br/>while (position < text.length()) {  
int end = Math._min_(position + chunkSize, text.length());  
String chunkContent = text.substring(position, end);  
<br/>DocumentChunk chunk = createChunk(chunkContent, chunkIndex, sourceDocument);  
chunks.add(chunk);  
chunkIndex++;  
<br/>_// 移动到下一块的起始位置（减去重叠）  
_position = end - chunkOverlap;  
<br/>_// 防止死循环（当 chunkOverlap >= chunkSize 时）  
_if (position >= end) {  
break;  
}  
}  
<br/>return chunks;  
}  
<br/>_/\*\*  
\* 获取文本末尾的重叠部分  
\* 从一段文本的末尾截取指定长度的内容，作为下一个块的开头。  
\* 这样两个相邻块之间就有共享的重叠内容，保证语义连贯性。  
\*  
\* @param text 原文本  
\* @param overlap 要截取的重叠长度  
\* @return 重叠部分文本  
\*/  
_private String getOverlapText(String text, int overlap) {  
if (text == null || text.isEmpty() || overlap <= 0) {  
return "";  
}  
if (text.length() <= overlap) {  
return text;  
}  
_// 从末尾截取  
_return text.substring(text.length() - overlap);  
}  
<br/>_/\*\*  
\* 创建一个文档块对象  
\* 封装创建 DocumentChunk 的逻辑，设置基本元数据。  
\*/  
_private DocumentChunk createChunk(String content, int chunkIndex, String sourceDocument) {  
DocumentChunk chunk = new DocumentChunk();  
chunk.setContent(content.trim());  
chunk.setChunkIndex(chunkIndex);  
chunk.setSourceDocument(sourceDocument);  
return chunk;  
}  
}

### 4.3.6 知识库构建服务类KnowledgeBaseService

知识库（Knowledge Base）就是把企业的文档、资料、知识等结构化地存储起来，  
\* 让 AI 能够快速检索和利用。在 RAG（检索增强生成）架构中，  
\* 知识库是"检索"那一步的数据来源。

package com.whlg.ai.demo.service;  
<br/>import com.whlg.ai.demo.dto.DocumentChunk;  
import com.whlg.ai.demo.dto.KnowledgeBaseBuildRequest;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.ai.document.Document;  
import org.springframework.ai.vectorstore.SearchRequest;  
import org.springframework.ai.vectorstore.VectorStore;  
import org.springframework.stereotype.Service;  
<br/>import java.io.File;  
import java.util.\*;  
<br/>_/\*\*  
\* 知识库构建服务  
\*  
\* 什么是知识库？  
\* 知识库（Knowledge Base）就是把企业的文档、资料、知识等结构化地存储起来，  
\* 让 AI 能够快速检索和利用。在 RAG（检索增强生成）架构中，  
\* 知识库是"检索"那一步的数据来源。  
\*  
\* 构建流程总览  
\* ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────────┐  
\* │ 文档解析 │ → │ 文本分块 │ → │ 向量化 │ → │ 向量库存储 │  
\* │ (Parser) │ │ (Splitter) │ │ (Embedding) │ │ (VectorStore)│  
\* └─────────────┘ └─────────────┘ └─────────────┘ └──────────────┘  
\*  
\* 1. 文档解析：把 PDF/Word/TXT 等格式的文档变成纯文本  
\* 2. 文本分块：把长文本切成合适大小的块（Chunk）  
\* 3. 向量化：把每个文本块变成向量（Embedding）—— 调用 AI 模型  
\* 4. 存储：把向量和原文一起存入 Chroma 向量数据库  
\*/  
_@Service  
public class KnowledgeBaseService {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(KnowledgeBaseService.class);  
<br/>_/\*\*  
\* 文档解析服务  
\*/  
_private final DocumentParserService documentParserService;  
<br/>_/\*\*  
\* 文本分块服务  
\*/  
_private final TextSplitterService textSplitterService;  
<br/>_/\*\*  
\* 向量存储（Spring AI 提供的抽象接口，底层是 Chroma）  
\* Spring AI 的 VectorStore 是一个统一的向量存储抽象，  
\* 支持 Milvus、Chroma、PGVector、Redis 等多种后端。  
\* 我们配置了 Chroma，所以这里注入的就是 ChromaVectorStore。  
\*/  
_private final VectorStore vectorStore;  
<br/>_// 构造函数注入  
_public KnowledgeBaseService(DocumentParserService documentParserService,  
TextSplitterService textSplitterService,  
VectorStore vectorStore) {  
this.documentParserService = documentParserService;  
this.textSplitterService = textSplitterService;  
this.vectorStore = vectorStore;  
}  
<br/>_/\*\*  
\* 构建知识库  
\* 这是知识库构建的主入口方法。  
\* 根据请求参数，处理指定的文档或目录，将内容向量化后存入 Chroma。  
\*  
\* @param request 构建请求参数  
\* @return 构建结果（成功处理的文档数和块数）  
\* @throws Exception 构建过程中出现的异常  
\*/  
_public BuildResult buildKnowledgeBase(KnowledgeBaseBuildRequest request) throws Exception {  
_log_.info("========== 开始构建知识库 ==========");  
_log_.info("文档路径: {}", request.getDocumentPath());  
_log_.info("分块大小: {} | 重叠大小: {}", request.getChunkSize(), request.getChunkOverlap());  
<br/>String documentPath = request.getDocumentPath();  
int chunkSize = request.getChunkSize();  
int chunkOverlap = request.getChunkOverlap();  
<br/>if (documentPath == null || documentPath.isBlank()) {  
throw new IllegalArgumentException("文档路径不能为空");  
}  
<br/>File pathFile = new File(documentPath);  
if (!pathFile.exists()) {  
throw new IllegalArgumentException("路径不存在: " + documentPath);  
}  
<br/>_// ========== 第一步：收集所有文档块 ==========  
_List&lt;DocumentChunk&gt; allChunks = new ArrayList<>();  
<br/>if (pathFile.isDirectory()) {  
_// 是目录，批量解析  
log_.info("检测到目录，开始批量处理...");  
List&lt;DocumentParserService.ParsedDocument&gt; docs =  
documentParserService.parseDirectory(documentPath);  
<br/>for (DocumentParserService.ParsedDocument doc : docs) {  
List&lt;DocumentChunk&gt; chunks = textSplitterService.splitText(  
doc.content(), chunkSize, chunkOverlap, doc.fileName());  
allChunks.addAll(chunks);  
}  
} else {  
_// 是单个文件  
log_.info("检测到单个文件，开始处理...");  
String content = documentParserService.parseDocument(documentPath);  
String fileName = pathFile.getName();  
List&lt;DocumentChunk&gt; chunks = textSplitterService.splitText(  
content, chunkSize, chunkOverlap, fileName);  
allChunks.addAll(chunks);  
}  
<br/>_log_.info("文档解析和分块完成 | 共得到 {} 个文本块", allChunks.size());  
<br/>if (allChunks.isEmpty()) {  
throw new IllegalStateException("没有提取到任何文本块，请检查文档内容");  
}  
<br/>_// ========== 第二步：转换为 Spring AI 的 Document 对象 ==========  
// Spring AI 的 VectorStore 接受的是它自己的 Document 类型  
_List&lt;Document&gt; documents = convertToSpringAiDocuments(allChunks);  
_log_.info("准备向量化并存入 Chroma | 文档数: {}", documents.size());  
<br/>_// ========== 第三步：向量化并写入 Chroma ==========  
// 这一步会自动调用 Embedding 模型生成向量，然后存入 Chroma  
// VectorStore.add() 方法内部做了两件事：  
// 1. 调用 EmbeddingModel 把文本变成向量  
// 2. 把向量和元数据写入 Chroma  
_try {  
vectorStore.add(documents);  
_log_.info("向量写入 Chroma 成功！共写入 {} 条记录", documents.size());  
} catch (Exception e) {  
_log_.error("写入 Chroma 失败", e);  
throw new RuntimeException("写入向量数据库失败: " + e.getMessage(), e);  
}  
<br/>_log_.info("========== 知识库构建完成 ==========");  
<br/>return new BuildResult(  
allChunks.size(),  
(int) allChunks.stream().map(DocumentChunk::getSourceDocument).distinct().count(),  
documentPath  
);  
}  
<br/>_/\*\*  
\* 检索相似文档  
\* 根据用户的问题，在知识库中找到最相关的几个文本块。  
\* 这是 RAG 中"检索"那一步的核心方法。  
\*  
\* 相似度检索的原理  
\* 1. 把用户的问题也变成向量（Embedding）  
\* 2. 在 Chroma 中计算这个向量和所有文档向量的相似度  
\* 3. 返回最相似的前 K 个文档  
\*  
\* @param query 用户的问题  
\* @param topK 返回最相似的前 K 个结果  
\* @return 相似的文档块列表  
\*/  
_public List&lt;DocumentChunk&gt; searchSimilar(String query, int topK) {  
_log_.info("开始相似性检索 | 查询: {} | TopK: {}", query, topK);  
<br/>if (query == null || query.isBlank()) {  
return new ArrayList<>();  
}  
if (topK <= 0) {  
topK = 4; _// 默认返回前 4 个  
_}  
<br/>_// 调用 Spring AI 的 VectorStore 进行相似度检索  
// 使用 SearchRequest 构建检索请求  
// similaritySearch 内部会：  
// 1. 把 query 向量化  
// 2. 在 Chroma 中做相似性搜索  
// 3. 返回最相似的文档  
_SearchRequest request = SearchRequest._builder_()  
.query(query)  
.topK(topK)  
.build();  
List&lt;Document&gt; results = vectorStore.similaritySearch(request);  
<br/>_// 转换为我们自己的 DocumentChunk 格式  
_List&lt;DocumentChunk&gt; chunks = new ArrayList<>();  
for (Document doc : results) {  
DocumentChunk chunk = new DocumentChunk();  
chunk.setId(doc.getId());  
chunk.setContent(doc.getText());  
chunk.setMetadata(doc.getMetadata());  
<br/>_// 从元数据中提取来源和块索引  
_Object source = doc.getMetadata().get("source");  
if (source != null) {  
chunk.setSourceDocument(source.toString());  
}  
Object idx = doc.getMetadata().get("chunk_index");  
if (idx != null) {  
chunk.setChunkIndex(Integer._parseInt_(idx.toString()));  
}  
<br/>chunks.add(chunk);  
}  
<br/>_log_.info("相似性检索完成 | 返回 {} 条结果", chunks.size());  
return chunks;  
}  
<br/>_/\*\*  
\* 将自定义 DocumentChunk 转换为 Spring AI 的 Document  
\* Spring AI 的 VectorStore 接口使用它自己的 Document 类，  
\* 所以我们需要把自己的数据结构转换过去。  
\*  
\* 为什么需要元数据（Metadata）？  
\* 向量数据库里存的不只是向量，还会存原文和一些附加信息（元数据）。  
\* 元数据的作用：  
\* - 检索后可以知道这段内容来自哪篇文档  
\* - 可以按来源、分类等条件过滤检索结果  
\* - 方便溯源和调试  
\*/  
_private List&lt;Document&gt; convertToSpringAiDocuments(List&lt;DocumentChunk&gt; chunks) {  
List&lt;Document&gt; documents = new ArrayList<>();  
<br/>for (DocumentChunk chunk : chunks) {  
_// 构建元数据 Map  
_Map&lt;String, Object&gt; metadata = new HashMap<>();  
metadata.put("source", chunk.getSourceDocument());  
metadata.put("chunk_index", chunk.getChunkIndex());  
metadata.put("content_length", chunk.getContent().length());  
<br/>_// 如果 chunk 自己有 metadata，也合并进去  
_if (chunk.getMetadata() != null) {  
metadata.putAll(chunk.getMetadata());  
}  
<br/>_// 创建 Spring AI 的 Document 对象  
// 注意：Spring AI 的 Document 会自动生成唯一 ID  
_Document doc = new Document(chunk.getContent(), metadata);  
documents.add(doc);  
}  
<br/>return documents;  
}  
<br/>_/\*\*  
\* 构建结果记录  
\* 封装知识库构建的结果信息，用于返回给前端。  
\*  
\* @param totalChunks 总共处理的文本块数量  
\* @param totalDocuments 总共处理的文档数量  
\* @param sourcePath 处理的源路径  
\*/  
_public record BuildResult(  
int totalChunks,  
int totalDocuments,  
String sourcePath  
) {}  
}

### 4.3.7 知识库控制器，含多个接口

package com.whlg.ai.demo.controller;  
<br/>import com.whlg.ai.demo.dto.DocumentChunk;  
import com.whlg.ai.demo.dto.KnowledgeBaseBuildRequest;  
import com.whlg.ai.demo.service.KnowledgeBaseService;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.http.ResponseEntity;  
import org.springframework.web.bind.annotation.\*;  
<br/>import java.util.List;  
import java.util.Map;  
<br/>_/\*\*  
\* 知识库控制器  
\* 提供知识库构建和检索的 REST API 接口。  
\*  
\* 接口列表  
\* - POST /api/knowledge/build - 构建知识库  
\* - GET /api/knowledge/search - 相似性检索  
\* - POST /api/knowledge/demo - 一键演示（使用内置示例文档）  
\*  
\* 什么是 RAG？  
\* RAG（Retrieval-Augmented Generation，检索增强生成）是一种让大模型  
\* 结合外部知识库来回答问题的技术。它的工作流程是：  
\*  
\* 用户问题 → 检索知识库 → 找到相关内容 → 拼接到 Prompt 中 → 大模型生成回答  
\*  
\* 本 Controller 提供的就是"检索知识库"这一步的接口。  
\*/  
_@RestController  
@RequestMapping("/api/knowledge")  
public class KnowledgeBaseController {  
<br/>private static final Logger _log_ \= LoggerFactory._getLogger_(KnowledgeBaseController.class);  
<br/>private final KnowledgeBaseService knowledgeBaseService;  
<br/>public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {  
this.knowledgeBaseService = knowledgeBaseService;  
}  
<br/>_/\*\*  
\* 构建知识库接口  
\* 解析指定的文档或目录，向量化后存入 Chroma 向量数据库。  
\*  
\* 请求示例：  
\* POST /api/knowledge/build  
\* Content-Type: application/json  
\*  
\* {  
\* "documentPath": "D:/docs/company_intro.pdf",  
\* "chunkSize": 500,  
\* "chunkOverlap": 50  
\* }  
\*  
\* 响应示例：  
\* {  
\* "success": true,  
\* "totalChunks": 42,  
\* "totalDocuments": 3,  
\* "sourcePath": "D:/docs/"  
\* }  
\*  
\* @param request 构建请求参数  
\* @return 构建结果  
\*/  
_@PostMapping("/build")  
public ResponseEntity&lt;Map<String, Object&gt;> build(@RequestBody KnowledgeBaseBuildRequest request) {  
_log_.info("收到知识库构建请求 | 路径: {}", request.getDocumentPath());  
<br/>try {  
KnowledgeBaseService.BuildResult result = knowledgeBaseService.buildKnowledgeBase(request);  
<br/>return ResponseEntity._ok_(Map._of_(  
"success", true,  
"totalChunks", result.totalChunks(),  
"totalDocuments", result.totalDocuments(),  
"sourcePath", result.sourcePath(),  
"message", "知识库构建成功！"  
));  
} catch (Exception e) {  
_log_.error("知识库构建失败", e);  
return ResponseEntity._internalServerError_().body(Map._of_(  
"success", false,  
"error", e.getMessage()  
));  
}  
}  
<br/>_/\*\*  
\* 相似性检索接口  
\* 根据用户的问题，在知识库中找到最相关的文本块。  
\* 这是 RAG 系统中"检索"那一步的核心接口。  
\*  
\* 请求示例：  
\* GET /api/knowledge/search?query=公司的产品有哪些&topK=5  
\*  
\* 响应示例：  
\* {  
\* "success": true,  
\* "results": \[  
\* {  
\* "id": "abc123",  
\* "content": "启云智能平台主要产品包括...",  
\* "sourceDocument": "公司介绍.pdf",  
\* "chunkIndex": 2,  
\* "metadata": {...}  
\* }  
\* \]  
\* }  
\*  
\* @param query 用户的查询问题  
\* @param topK 返回最相似的前 K 个结果（默认 4）  
\* @return 相似文档块列表  
\*/  
_@GetMapping("/search")  
public ResponseEntity&lt;Map<String, Object&gt;> search(  
@RequestParam("query") String query,  
@RequestParam(value = "topK", defaultValue = "4") int topK) {  
_log_.info("收到检索请求 | 查询: {} | TopK: {}", query, topK);  
<br/>try {  
List&lt;DocumentChunk&gt; results = knowledgeBaseService.searchSimilar(query, topK);  
<br/>return ResponseEntity._ok_(Map._of_(  
"success", true,  
"total", results.size(),  
"results", results  
));  
} catch (Exception e) {  
_log_.error("检索失败", e);  
return ResponseEntity._internalServerError_().body(Map._of_(  
"success", false,  
"error", e.getMessage()  
));  
}  
}  
<br/>_/\*\*  
\* 一键演示接口  
\* 使用内置的示例文档快速演示知识库构建和检索功能。  
\* 方便学习者快速验证整个流程是否正常工作。  
\*  
\* 演示内容  
\* 1. 读取 classpath 下的示例文档  
\* 2. 解析并分块  
\* 3. 向量化并存入 Chroma  
\* 4. 执行一个示例检索  
\* 5. 返回完整的演示结果  
\*  
\* @return 演示结果  
\*/  
_@PostMapping("/demo")  
public ResponseEntity&lt;Map<String, Object&gt;> demo() {  
_log_.info("收到一键演示请求");  
<br/>try {  
_// 从 classpath 获取示例文档路径  
_String demoDocPath = getClass().getClassLoader().getResource("sample-docs") != null  
? getClass().getClassLoader().getResource("sample-docs").getPath()  
: null;  
<br/>if (demoDocPath == null) {  
return ResponseEntity._badRequest_().body(Map._of_(  
"success", false,  
"message", "未找到示例文档目录 sample-docs，请确保资源文件存在"  
));  
}  
<br/>_// Windows 下路径可能以 / 开头，需要处理  
_if (demoDocPath.startsWith("/") && demoDocPath.contains(":")) {  
demoDocPath = demoDocPath.substring(1);  
}  
_// URL 解码（处理路径中的空格和中文）  
_demoDocPath = java.net.URLDecoder._decode_(demoDocPath, "UTF-8");  
<br/>_log_.info("示例文档路径: {}", demoDocPath);  
<br/>_// ========== 第一步：构建知识库 ==========  
_KnowledgeBaseBuildRequest buildRequest = new KnowledgeBaseBuildRequest();  
buildRequest.setDocumentPath(demoDocPath);  
buildRequest.setChunkSize(500);  
buildRequest.setChunkOverlap(50);  
<br/>KnowledgeBaseService.BuildResult buildResult =  
knowledgeBaseService.buildKnowledgeBase(buildRequest);  
<br/>_// ========== 第二步：执行示例检索 ==========  
_String sampleQuery = "启云智能平台有哪些功能？";  
List&lt;DocumentChunk&gt; searchResults =  
knowledgeBaseService.searchSimilar(sampleQuery, 3);  
<br/>_log_.info("演示完成 | 构建 {} 个块 | 检索返回 {} 条结果",  
buildResult.totalChunks(), searchResults.size());  
<br/>return ResponseEntity._ok_(Map._of_(  
"success", true,  
"message", "知识库构建与检索演示成功！",  
"buildResult", Map._of_(  
"totalChunks", buildResult.totalChunks(),  
"totalDocuments", buildResult.totalDocuments(),  
"sourcePath", buildResult.sourcePath()  
),  
"searchDemo", Map._of_(  
"query", sampleQuery,  
"resultsCount", searchResults.size(),  
"topResult", searchResults.isEmpty() ? null : searchResults.get(0).getContent()  
)  
));  
<br/>} catch (Exception e) {  
_log_.error("演示失败", e);  
return ResponseEntity._internalServerError_().body(Map._of_(  
"success", false,  
"error", e.getMessage(),  
"hint", "请确保 Chroma 服务已启动，并且配置正确"  
));  
}  
}  
}

## 4.4 接口测试

### 4.4.1 构建知识库

**接口：** POST /api/knowledge/build

**请求体：**

  
{  
 "documentPath": "D:/docs/",     // 文档路径（文件或目录）  
 "chunkSize": 500,                // 分块大小（字符数，可选，默认500）  
 "chunkOverlap": 50,              // 重叠大小（字符数，可选，默认50）  
 "collectionName": "my_kb"        // Milvus集合名（可选）  
}

**响应：**

  
{  
 "success": true,  
 "totalChunks": 42,  
 "totalDocuments": 3,  
 "sourcePath": "D:/docs/",  
 "message": "知识库构建成功！"  
}

### 4.4.2 相似性检索

**接口：** GET /api/knowledge/search

**参数：**

- query（必填） - 查询的问题
- topK（可选，默认4） - 返回最相似的前K个结果

**响应：**

{  
 "success": true,  
 "total": 3,  
 "results": \[  
  {  
     "id": "abc123",  
     "content": "启云智能平台主要产品包括...",  
     "sourceDocument": "公司介绍.pdf",  
     "chunkIndex": 2,  
  }  
\]  
}

### 4.4.3 一键演示

**接口：** POST /api/knowledge/demo

**说明：** 使用内置示例文档自动完成构建和检索，方便快速验证

## 4.5 删除集合及与MySQL的对比

如果要在Chroma向量库中删除集合，可直接调用 Chroma API 删除指定的集合：

curl -X DELETE http://localhost:8000/api/v1/collections/qiyun_knowledge_base

删完后重启应用，initialize-schema: true 会自动重建空集合。

qiyun_knowledge_base 就相当于 MySQL 里的一张表，里面存的是向量化的文档块。你可以建多个集合放不同用途的数据，互不干扰。