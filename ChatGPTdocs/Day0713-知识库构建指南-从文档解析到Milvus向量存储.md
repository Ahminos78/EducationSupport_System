# 知识库构建完整教程：从文档解析到 Milvus 向量存储

> 本文档面向初学者，手把手教你如何在 Spring Boot 项目中实现企业知识库构建功能。
> 代码基于 Spring AI + DeepSeek + Milvus 技术栈。

---

## 📚 目录

1. [核心概念入门](#1-核心概念入门)
2. [整体架构与流程](#2-整体架构与流程)
3. [环境准备（从零开始安装到启动）](#3-环境准备从零开始安装到启动)
   - [📋 方案对比](#-方案对比)
   - [方案一：Docker + Milvus（功能最全）](#方案一docker--milvus功能最全)
   - [方案二：Docker + Chroma（更简单）](#方案二docker--chroma更简单)
   - [方案三：内存向量存储（零安装）⭐](#方案三内存向量存储零安装-强烈推荐)
   - 3.4 [通用依赖](#34-通用依赖)
   - 3.5 [通用配置](#35-通用配置)
   - 3.6 [启动 Spring Boot 项目](#36-启动-spring-boot-项目)
   - 3.7 [验证接口](#37-验证接口)
4. [第一步：文档解析（Document Parser）](#4-第一步文档解析document-parser)
5. [第二步：文本分块（Text Chunking）](#5-第二步文本分块text-chunking)
6. [第三步：向量化（Embedding）](#6-第三步向量化embedding)
7. [第四步：向量存储原理](#7-第四步向量存储原理)
8. [完整演示：一键运行](#8-完整演示一键运行)
9. [API 接口说明](#9-api-接口说明)
10. [常见问题 FAQ](#10-常见问题-faq)

---

## 1. 核心概念入门

在动手写代码之前，让我们先搞清楚几个关键概念。

### 1.1 什么是 RAG？

**RAG（Retrieval-Augmented Generation，检索增强生成）** 是一种让大模型结合外部知识库来回答问题的技术。

**为什么需要 RAG？**
- 大模型的知识是"过时"的：训练数据截止到某个时间点，不知道最新信息
- 大模型不知道企业内部的知识：比如公司制度、产品文档、客户资料等
- 大模型"幻觉"（Hallucination）：编造看起来很真实但实际错误的答案，一本正经的胡说八道 

**RAG 的工作流程：**
```
用户问题 → 检索知识库 → 找到相关内容 → 拼接到 Prompt 中 → 大模型生成回答
                                                              ↑
                                                答案基于真实的知识库，更准确！
```

### 1.2 什么是向量（Vector）？

**向量**就是一串数字，比如 `[0.12, -0.34, 0.56, ...]`。

在 AI 领域，我们可以把一段文本变成一个向量（这个过程叫**向量化**或**Embedding**）。神奇的是：

> **语义相似的文本，它们的向量在数学空间中也离得很近。**

比如：
- "苹果手机怎么样？" 和 "iPhone 好用吗？" → 向量距离很近（语义相似）
- "苹果手机怎么样？" 和 "香蕉多少钱一斤？" → 向量距离很远（语义不同）

### 1.3 什么是向量数据库？

**向量数据库**是专门用来存储和查询向量的数据库。

| 传统数据库 | 向量数据库 |
|-----------|-----------|
| 存的是表格、文字 | 存的是向量（一串数字） |
| 查询用 SQL（精确匹配） | 查询用"相似度"（找最像的） |
| 比如 MySQL、PostgreSQL | 比如 Milvus、Chroma、Qdrant |

**为什么需要向量数据库？**
- 当知识库有几十万、几百万条文本时，不可能每条都去算一遍相似度
- 向量数据库专门做了索引优化，能在毫秒级返回最相似的结果

### 1.4 什么是 Milvus？

**Milvus** 是一款开源的向量数据库，特点是：
- 🚀 性能高：支持十亿级向量的毫秒级检索
- 📦 功能全：支持多种索引类型、相似度计算方式
- 🐳 易部署：Docker 一键启动
- 🔧 生态好：方便集成 LangChain、Spring AI 等框架

### 1.5 什么是文本分块（Chunking）

**文本分块**就是把一篇长文档切成很多个小块（Chunk）。

**为什么要切分？**
1. **上下文窗口限制**：大模型能处理的 Token 数量有限，不能把整本书都塞进去
2. **检索精度**：小块的语义更集中，更容易找到最相关的内容
3. **性能效率**：小块向量化更快，存储和检索也更高效

**什么是重叠（Overlap）？**

想象一下切面包，如果每一刀切得太干脆，可能会把一颗葡萄干切成两半。重叠就是让相邻的两块共享一部分内容，保证语义不会在切分点断裂。

```
块1: [Hello, this is a sample text for]
块2:          [text for chunking demonstration. It shows]
                ↑ 重叠部分（Overlap）
```

---

## 2. 整体架构与流程

### 2.1 技术栈

| 技术 | 版本/说明 | 作用 |
|------|----------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.0 | 后端框架 |
| Spring AI | 1.0.0-M7 | AI 应用开发框架（封装了模型调用、向量存储等） |
| DeepSeek | text-embedding-v2 | 向量化模型（兼容 OpenAI API） |
| Milvus | 2.x | 向量数据库 |
| Apache PDFBox | 2.0.32 | PDF 文档解析 |
| Apache POI | 5.2.5 | Word 文档解析 |

### 2.2 完整流程

知识库构建的完整流程分为 4 步：

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  文档解析   │ →  │  文本分块   │ →  │  向量化    │ →  │ Milvus存储 │
│ (Parser)    │    │ (Splitter)  │    │ (Embedding)│    │ (VectorStore)│
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
     ↓                  ↓                  ↓                  ↓
  PDF/Word/TXT     长文本→小块        文本→向量         向量+原文存入数据库
  → 纯文本         (500字符/块)      (1536维向量)        (可相似度检索)
```

1. **文档解析**：把 PDF、Word、TXT 等格式的文档提取成纯文本
2. **文本分块**：把长文本切成合适大小的块（每块约 500 字符，带重叠）
3. **向量化**：调用 Embedding 模型，把每个文本块变成向量
4. **存储**：把向量和原文一起存入 Milvus 向量数据库

---

## 3. 环境准备（从零开始安装到启动）

> **重要说明**：Milvus **没有 Windows 原生安装包**（.exe 或 .msi 文件），官方只提供 Docker 镜像和 Linux 版本。对于 Java 项目，我们有以下三种方案可选，**推荐初学者使用方案三（内存存储，零安装）**，等熟悉后再升级到 Docker 方案。

### 📋 方案对比

| 方案 | 需要安装 | 难度 | 数据持久化 | 推荐度 |
|------|---------|------|-----------|--------|
| **方案一：Docker + Milvus** | Docker Desktop | ⭐⭐⭐ | ✅ | 生产环境 |
| **方案二：Docker + Chroma** | Docker Desktop | ⭐⭐ | ✅ | 开发测试 |
| **方案三：内存向量存储** | 什么都不用 | ⭐ | ❌ | 学习入门 ⭐ |

---

### 方案一：Docker + Milvus（功能最全）

#### 3.1.1 安装 Docker Desktop

**步骤 1：下载**

访问 https://www.docker.com/products/docker-desktop/ 下载 Windows 版本。

**步骤 2：安装**

1. 双击 `.exe` 安装包，一路点击 "Next"
2. **关键**：勾选 **"Use WSL 2 instead of Hyper-V"**
3. 点击 "Finish"，**重启电脑**

**步骤 3：验证**

重启后打开 命令行窗口，输入下面的指令：

```powershell
docker --version
docker-compose --version
```

看到版本号说明安装成功。

#### 3.1.2 启动 Milvus

> **重要说明：你不需要手动"安装" Milvus！**
>
> Docker 的工作方式和你平时安装软件不一样：
> - 传统方式：下载安装包 → 双击安装 → 配置环境 → 启动服务
> - Docker 方式：写个配置文件 → 运行一条命令 → Docker 自动从网上下载镜像并启动
>
> 就像点外卖：你在饿了么App 下单后，外卖员会把午饭送到你家。Docker 就是那个"外卖员"。

**步骤 1：创建工作目录**

这个目录用来存放 Docker 的配置文件，我们自己手动创建：

```powershell
mkdir D:\milvus-docker
cd D:\milvus-docker
```

**步骤 2：创建配置文件**

在 `D:\milvus-docker` 目录下，**手动新建一个文本文件**，命名为 `docker-compose.yml`（注意后缀名是 `.yml`，不是 `.txt`）。

然后把以下内容完整复制进去：

```yaml
version: '3.5'
services:
  etcd:
    container_name: milvus-etcd
    image: quay.io/coreos/etcd:v3.5.5
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
    volumes:
      - ./volumes/etcd:/etcd
    command: etcd -advertise-client-urls=http://127.0.0.1:2379 -listen-client-urls http://0.0.0.0:2379 --data-dir /etcd

  minio:
    container_name: milvus-minio
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    volumes:
      - ./volumes/minio:/minio_data
    command: minio server /minio_data

  standalone:
    container_name: milvus-standalone
    image: milvusdb/milvus:v2.3.0
    command: ["milvus", "run", "standalone"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - ./volumes/milvus:/var/lib/milvus
    ports:
      - "19530:19530"
    depends_on:
      - "etcd"
      - "minio"
```

**步骤 3：启动（Docker 会自动下载 Milvus）**

确保当前目录是 `D:\milvus-docker`，然后执行：

```powershell
docker-compose up -d
```

**第一次运行会发生什么呢？**

1. Docker 会检查本地有没有 Milvus、etcd、minio 的镜像
2. 如果没有，Docker 会自动从网络下载（Pull）
3. 下载完成后，自动按配置启动三个容器

**首次下载约需 5-10 分钟**（取决于网速），你会看到类似这样的输出：

```
Pulling etcd (quay.io/coreos/etcd:v3.5.5)...
v3.5.5: Pulling from coreos/etcd
...
Pulling standalone (milvusdb/milvus:v2.3.0)...
v2.3.0: Pulling from milvusdb/milvus
...
Status: Downloaded newer image for milvusdb/milvus:v2.3.0
Creating milvus-etcd      ... done
Creating milvus-minio     ... done
Creating milvus-standalone ... done
```

> **提示**：如果下载很慢，可以配置 Docker 镜像加速器（见下方"常见问题"）。

**步骤 4：验证**

```powershell
docker-compose ps
```

看到三个容器都是 `Up` 状态即可。

**重要说明：Milvus 默认没有密码！**

我们用的是 Milvus 单机模式（Standalone），默认情况下：
- ✅ **不需要用户名和密码**（用户名密码认证默认是关闭的）
- ✅ **端口是 19530**（Milvus 默认端口）
- ❌ **不要配置 username 和 password**，否则会连接失败

只有在**集群模式**或**显式启用认证**时才需要用户名密码。

**步骤 5：停止（可选）**

```powershell
docker-compose down
```

#### 3.1.3 修改项目依赖和配置

**pom.xml：**

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-milvus</artifactId>
</dependency>
```

**application.yml：**

```yaml
spring:
  ai:
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
          username: root
          password: Milvus
        collectionName: qiyun_knowledge_base
        embeddingDimension: 1536
        initialize-schema: true
```

---

### 方案二：Docker + Chroma（更简单，数据可持久化）

Chroma 是另一个向量数据库，比 Milvus 轻量很多，**只需要一个 Docker 命令**就能启动。

#### 3.2.1 安装 Docker Desktop

同方案一的 3.1.1 节。

#### 3.2.2 启动 Chroma

**只需要一条命令：**

```powershell
docker run -d -p 8000:8000 --name chroma ghcr.io/chroma-core/chroma:latest
```

验证：

```powershell
docker ps
```

看到 `chroma` 容器在运行即可。

#### 3.2.3 修改项目依赖和配置

**pom.xml：**

把 Milvus 依赖换成 Chroma：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-chroma-store-spring-boot-starter</artifactId>
</dependency>
```

**application.yml：**

```yaml
spring:
  ai:
    vectorstore:
      chroma:
        host: http://localhost:8000
```

> **注意**：Chroma 的 `application.yml` 配置比 Milvus 简单很多！

---

### 方案三：内存向量存储（零安装，最适合入门）⭐ 强烈推荐

如果你**不想安装任何东西**，只想快速看到效果，用这个方案。数据存储在内存中，重启后丢失，但**学习完全够用**。

#### 3.3.1 无需安装任何数据库

什么都不用装！直接跳过所有数据库安装步骤。

#### 3.3.2 修改项目依赖和配置

**pom.xml：**

```xml
<!-- 不需要 Milvus/Chroma 的依赖 -->
<!-- 只需要 Spring AI 的向量存储核心包 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-store-spring-boot-starter</artifactId>
</dependency>
```

**application.yml：**

```yaml
spring:
  ai:
    vectorstore:
      simple:
        enabled: true
```

> 就这几行，完事了！直接启动 Spring Boot 项目即可。

---

### 3.4 通用依赖（三种方案都需要）

无论选择哪种向量数据库方案，以下依赖都是必需的：

**pom.xml：**

```xml
<!-- Spring AI OpenAI（用于调用 DeepSeek Embedding） -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>

<!-- Apache PDFBox：解析 PDF 文档 -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.32</version>
</dependency>

<!-- Apache POI：解析 Word 文档（docx） -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 3.5 通用配置（三种方案都需要）

**application.yml：**

```yaml
server:
  port: 8083

spring:
  application:
    name: qiyun-ai-demo
  ai:
    openai:
      api-key: sk-你的API-Key
      base-url: https://api.deepseek.com
      chat:
        api-key: sk-你的API-Key
        options:
          model: deepseek-v4-pro
      embedding:
        api-key: sk-你的API-Key
        options:
          model: text-embedding-v2
    # 向量数据库配置根据你选择的方案填写（见上方）

logging:
  level:
    org.springframework.ai: INFO
    com.whlg.ai.demo: DEBUG
```

### 3.6 启动 Spring Boot 项目

配置完成后，启动项目：

**方式一：IDEA 中启动**

找到 `AiDemoApp.java`，右键 → Run。

**方式二：命令行启动**

```powershell
cd qiyun-ai-demo
mvn spring-boot:run
```

看到 `Started AiDemoApp in x.xxx seconds` 说明启动成功！

### 3.7 验证接口

打开浏览器或 Postman，测试接口：

```bash
# 测试知识库构建
curl -X POST http://localhost:8083/api/knowledge/demo

# 测试检索
curl "http://localhost:8083/api/knowledge/search?query=平台功能&topK=3"
```

---

## 4. 第一步：文档解析（Document Parser）

### 4.1 功能说明

文档解析的目标：把各种格式的企业文档，统一提取成纯文本字符串。

支持的格式：
- 📄 `.txt` - 纯文本（最简单）
- 📑 `.pdf` - PDF 文档（用 Apache PDFBox）
- 📝 `.docx` - Word 文档（用 Apache POI）

### 4.2 核心代码

创建 `DocumentParserService.java`：

```java
@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    /**
     * 解析单个文档
     * 根据文件扩展名自动选择解析器
     */
    public String parseDocument(String filePath) throws IOException {
        File file = new File(filePath);
        
        // 检查文件是否存在
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        String fileName = file.getName().toLowerCase();
        log.info("开始解析文档: {}", fileName);
        
        // 根据扩展名选择解析方式
        String content;
        if (fileName.endsWith(".txt")) {
            content = parseTxt(file);
        } else if (fileName.endsWith(".pdf")) {
            content = parsePdf(file);
        } else if (fileName.endsWith(".docx")) {
            content = parseDocx(file);
        } else {
            throw new IllegalArgumentException("不支持的文档格式: " + fileName);
        }
        
        log.info("文档解析完成 | 字符数: {}", content.length());
        return content;
    }

    /**
     * 解析 TXT 文件
     */
    private String parseTxt(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * 解析 PDF 文档
     * 使用 Apache PDFBox
     */
    private String parsePdf(File file) throws IOException {
        // try-with-resources 自动关闭资源
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);  // 按阅读顺序提取
            String text = stripper.getText(document);
            return cleanText(text);
        }
    }

    /**
     * 解析 Word 文档（.docx）
     * 使用 Apache POI
     */
    private String parseDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            return cleanText(text);
        }
    }

    /**
     * 清理文本：去除多余空白、空行
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) return "";
        
        // 统一换行符
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        // 压缩多个空行
        text = text.replaceAll("\\n{3,}", "\n\n");
        
        // 去除每行首尾空白
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(trimmed).append("\n");
            }
        }
        return result.toString().trim();
    }
    
    // 批量解析目录的方法...
}
```

### 4.3 知识点讲解

**1. try-with-resources 语法**

```java
try (PDDocument document = PDDocument.load(file)) {
    // 使用 document
} // 这里自动关闭，不用写 finally
```

这是 Java 7+ 的语法，只要类实现了 `AutoCloseable` 接口，就可以用这种方式自动关闭资源，避免资源泄漏。

**2. 为什么要清理文本？**

PDF 和 Word 提取的文本经常有大量无用的空格、换行、分页符等，需要清理后再使用，否则会影响后续的分块和向量化质量。

---

## 5. 第二步：文本分块（Text Chunking）

### 5.1 功能说明

文本分块的目标：把一整篇长文档，切分成多个合适大小的文本块。

**分块策略：**
- 优先按段落切分（保持语义完整）
- 段落太长就按句子切分
- 句子还太长就按字符硬切（最后手段）
- 相邻块之间有重叠（Overlap），避免语义断裂

### 5.2 核心代码

创建 `TextSplitterService.java`：

```java
@Service
public class TextSplitterService {

    // 默认块大小（字符数）
    public static final int DEFAULT_CHUNK_SIZE = 500;
    // 默认重叠大小（字符数）
    public static final int DEFAULT_CHUNK_OVERLAP = 50;

    /**
     * 将文本切分为多个块
     * 
     * @param text           完整文本
     * @param chunkSize      每块大小（字符数）
     * @param chunkOverlap   重叠大小（字符数）
     * @param sourceDocument 来源文档名
     * @return 文档块列表
     */
    public List<DocumentChunk> splitText(String text, int chunkSize, 
                                          int chunkOverlap, String sourceDocument) {
        // 参数校验
        if (text == null || text.isEmpty()) return new ArrayList<>();
        if (chunkSize <= 0) chunkSize = DEFAULT_CHUNK_SIZE;
        if (chunkOverlap < 0) chunkOverlap = DEFAULT_CHUNK_OVERLAP;
        if (chunkOverlap >= chunkSize) chunkOverlap = chunkSize / 10;

        List<DocumentChunk> chunks = new ArrayList<>();
        
        // 第一步：按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            // 如果加上这个段落还没超限，就继续累积
            if (currentChunk.length() + paragraph.length() + 2 <= chunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                // 超限了，先把当前块存起来
                if (currentChunk.length() > 0) {
                    chunks.add(createChunk(currentChunk.toString(), chunkIndex, sourceDocument));
                    chunkIndex++;
                    
                    // 计算重叠部分，作为下一个块的开头
                    String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);
                    currentChunk = new StringBuilder(overlapText);
                }

                // 如果单个段落就超过了块大小，需要更细粒度切分
                if (paragraph.length() > chunkSize) {
                    List<DocumentChunk> subChunks = splitLongParagraph(
                            paragraph, chunkSize, chunkOverlap, sourceDocument, chunkIndex);
                    chunks.addAll(subChunks);
                    chunkIndex += subChunks.size();
                    // ...
                } else {
                    currentChunk.append(paragraph);
                }
            }
        }

        // 处理最后一块
        if (currentChunk.length() > 0) {
            chunks.add(createChunk(currentChunk.toString(), chunkIndex, sourceDocument));
        }

        return chunks;
    }

    /**
     * 获取文本末尾的重叠部分
     */
    private String getOverlapText(String text, int overlap) {
        if (text == null || text.isEmpty() || overlap <= 0) return "";
        if (text.length() <= overlap) return text;
        return text.substring(text.length() - overlap);
    }
    
    // 其他辅助方法...
}
```

### 5.3 分块策略的选择

**常见的分块策略对比：**

| 策略 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| 固定大小分块 | 简单、快速 | 可能切断句子 | 通用场景 |
| 按句子/段落分块 | 语义完整 | 块大小不均匀 | 文章、文档 |
| 递归字符分块 | 兼顾大小和语义 | 实现复杂 | 大多数场景（推荐） |
| 语义分块 | 最智能 | 需要模型、慢 | 高质量要求 |

我们实现的是"递归字符分块"的简化版本：段落 → 句子 → 字符。

### 5.4 块大小怎么选？

| 块大小 | 优点 | 缺点 | 适用场景 |
|--------|------|------|----------|
| 小（100-300） | 检索精准 | 上下文少，可能不完整 | FAQ、问答对 |
| 中（300-800） | 平衡精准和完整 | - | 通用场景（推荐） |
| 大（800-2000） | 上下文丰富 | 可能有噪音，检索不准 | 摘要、长文档理解 |

**建议：** 先用 500 字符 + 50 重叠试试效果，再根据实际情况调整。

---

## 6. 第三步：向量化（Embedding）

### 6.1 功能说明

向量化就是把文本变成向量的过程。

**直观理解：**
```
"启云智能平台有哪些功能？" 
    ↓ Embedding 模型
[0.1234, -0.5678, 0.9012, ..., 0.3456]  ← 1536 维的向量
```

### 6.2 Spring AI 的封装

好消息是，Spring AI 已经帮我们把向量化的过程封装好了！我们不需要手动调用 API。

在 `KnowledgeBaseService` 中，我们注入 `VectorStore`，调用 `vectorStore.add(documents)` 的时候，Spring AI 会自动：
1. 把文本传给 Embedding 模型
2. 拿到返回的向量
3. 把向量和原文一起存入 Milvus

```java
@Service
public class KnowledgeBaseService {
    
    // Spring AI 自动配置好的向量存储
    private final VectorStore vectorStore;
    
    public void buildKnowledgeBase(...) {
        // ... 解析和分省略 ...
        
        // 转换为 Spring AI 的 Document 对象
        List<Document> documents = convertToSpringAiDocuments(allChunks);
        
        // 这一步自动完成：向量化 + 存储
        vectorStore.add(documents);
    }
}
```

### 6.3 相似度怎么计算？

两个向量的相似度通常用**余弦相似度（Cosine Similarity）**来衡量：

- 取值范围：[-1, 1]
- 越接近 1，两个向量越相似
- 越接近 0，越不相关
- 负数表示语义相反

Milvus 支持多种相似度计算方式，我们在配置中选了 `COSINE`（余弦相似度）。

---

## 7. 第四步：向量存储原理

### 7.1 核心概念

**Milvus 中的几个重要概念：**

| 概念 | 类比关系型数据库 | 说明 |
|------|----------------|------|
| Collection | 表（Table） | 存放一组向量的集合 |
| Entity | 行（Row） | 一条数据（向量 + 标量字段） |
| Field | 列（Column） | 数据字段，分为向量字段和标量字段 |
| Index | 索引 | 加速向量检索的数据结构 |

### 7.2 Spring AI VectorStore 接口

Spring AI 提供了统一的 `VectorStore` 接口，屏蔽了不同向量数据库的差异：

```java
// VectorStore 接口的核心方法
public interface VectorStore {
    
    // 添加文档（自动向量化 + 存储）
    void add(List<Document> documents);
    
    // 按 ID 删除
    void delete(List<String> idList);
    
    // 相似度检索
    List<Document> similaritySearch(String query);
    
    // 相似度检索（指定返回数量）
    List<Document> similaritySearch(String query, int topK);
}
```

我们的代码中只需要注入 `VectorStore`，Spring Boot 会根据配置自动创建 Milvus 的实现。

### 7.3 检索是怎么工作的？

当调用 `vectorStore.similaritySearch(query, topK)` 时：

```
用户的问题 "平台有哪些功能？"
        ↓
    向量化处理（Embedding）
        ↓
    得到查询向量 Q
        ↓
    在 Milvus 中计算 Q 和所有文档向量的相似度
        ↓
    按相似度从高到低排序
        ↓
    返回前 K 个最相似的文档
```

整个过程是全自动的，Spring AI + Milvus 帮我们搞定了！

---

## 8. 完整演示：一键运行

### 8.1 示例文档

我们在 `src/main/resources/sample-docs/` 目录下放了两个示例文档：
- `qiyun-platform-intro.txt` - 启云智能平台产品介绍
- `qiyun-faq.txt` - 常见问题解答

### 8.2 一键演示接口

调用 `POST /api/knowledge/demo` 接口，一键完成：
1. 读取示例文档
2. 解析并分块
3. 向量化并存入 Milvus
4. 执行一个示例检索

**使用 curl 测试：**
```bash
curl -X POST http://localhost:8083/api/knowledge/demo
```

**预期返回：**
```json
{
  "success": true,
  "message": "知识库构建与检索演示成功！",
  "buildResult": {
    "totalChunks": 25,
    "totalDocuments": 2,
    "sourcePath": "..."
  },
  "searchDemo": {
    "query": "启云智能平台有哪些功能？",
    "resultsCount": 3,
    "topResult": "启云智能平台核心功能包括智能对话系统、知识库管理、智能客服系统..."
  }
}
```

### 8.3 手动构建知识库

如果你有自己的文档，可以用构建接口：

```bash
curl -X POST http://localhost:8083/api/knowledge/build \
  -H "Content-Type: application/json" \
  -d '{
    "documentPath": "D:/your-docs-folder/",
    "chunkSize": 500,
    "chunkOverlap": 50
  }'
```

### 8.4 测试检索

```bash
curl "http://localhost:8083/api/knowledge/search?query=支持哪些大模型&topK=5"
```

---

## 9. API 接口说明

### 9.1 构建知识库

**接口：** `POST /api/knowledge/build`

**请求体：**
```json
{
  "documentPath": "D:/docs/",     // 文档路径（文件或目录）
  "chunkSize": 500,                // 分块大小（字符数，可选，默认500）
  "chunkOverlap": 50,              // 重叠大小（字符数，可选，默认50）
  "collectionName": "my_kb"        // Milvus集合名（可选）
}
```

**响应：**
```json
{
  "success": true,
  "totalChunks": 42,
  "totalDocuments": 3,
  "sourcePath": "D:/docs/",
  "message": "知识库构建成功！"
}
```

### 9.2 相似性检索

**接口：** `GET /api/knowledge/search`

**参数：**
- `query`（必填） - 查询的问题
- `topK`（可选，默认4） - 返回最相似的前K个结果

**响应：**
```json
{
  "success": true,
  "total": 3,
  "results": [
    {
      "id": "abc123",
      "content": "启云智能平台主要产品包括...",
      "sourceDocument": "公司介绍.pdf",
      "chunkIndex": 2,
      "metadata": {...}
    }
  ]
}
```

### 9.3 一键演示

**接口：** `POST /api/knowledge/demo`

**说明：** 使用内置示例文档自动完成构建和检索，方便快速验证。

---

## 10. 常见问题 FAQ

### Q1：Milvus 启动失败怎么办？

**A：** 检查以下几点：
1. Docker 和 Docker Compose 是否正确安装
2. 端口 19530、9091、9000、9001、2379 是否被占用
3. 机器内存是否足够（建议至少 4GB）
4. 查看容器日志：`docker logs milvus-standalone`

### Q2：调用 Embedding API 报错怎么办？

**A：** 常见原因：
1. API Key 错误或过期 → 检查配置
2. 网络不通 → 确认能访问 `https://api.deepseek.com`
3. 模型名称不对 → DeepSeek 的 embedding 模型是 `text-embedding-v2`
4. 余额不足 → 去 DeepSeek 控制台检查

### Q3：向量维度不匹配怎么办？

**A：** 确保 `embeddingDimension` 配置和实际模型的输出维度一致：
- DeepSeek text-embedding-v2 → 1536 维
- OpenAI text-embedding-3-small → 1536 维
- OpenAI text-embedding-ada-002 → 1536 维

如果之前创建过集合但维度不对，需要删除旧集合重新创建。

### Q4：PDF 解析出来是乱码或空的？

**A：** 可能的原因：
1. 扫描版 PDF（图片型）→ 需要 OCR 识别后才能提取文字
2. PDF 加密了 → 需要提供密码
3. 特殊字体或编码 → 可以尝试用其他 PDF 解析库

### Q5：检索结果不准确怎么办？

**A：** 可以从以下几个方面优化：
1. **调整块大小**：试试更大或更小的 chunkSize
2. **调整重叠大小**：增加 overlap 可能提升召回率
3. **优化文档质量**：确保文档内容清晰、结构良好
4. **调整 TopK**：返回更多结果可能提高召回率
5. **使用更好的 Embedding 模型**：不同模型效果不同
6. **重排序（Rerank）**：检索后再用模型排序一遍

### Q6：可以支持更多文档格式吗？

**A：** 当然可以！你可以扩展 `DocumentParserService`：
- PPT → Apache POI 的 XSLF
- Excel → Apache POI 的 XSSF
- Markdown → 直接读取（本身就是纯文本）
- HTML → 用 Jsoup 解析
- 扫描版 PDF → 集成 OCR（如 Tesseract）

### Q7：生产环境需要注意什么？

**A：** 生产环境建议：
1. 关闭 `initialize-schema: true`，手动管理集合
2. 配置合适的索引参数（nlist、nprobe 等）
3. 做好数据备份
4. 加限流和监控
5. 考虑用 Milvus 集群版而不是单机版
6. API Key 不要硬编码，用配置中心或环境变量

---

## 🎉 总结

本文一共实现了：

1. ✅ **文档解析**：支持 TXT、PDF、Word 格式
2. ✅ **文本分块**：智能切分，带重叠保持上下文
3. ✅ **向量化**：基于 DeepSeek Embedding API
4. ✅ **Milvus 存储**：向量数据库存储与相似度检索
5. ✅ **完整演示**：一键运行的示例接口

这只是 RAG 系统的第一步（数据准备）。完整的 RAG 应用还包括：
- 查询改写（Query Rewriting）
- 检索重排序（Reranking）
- Prompt 工程
- 回答生成
- 引用溯源
