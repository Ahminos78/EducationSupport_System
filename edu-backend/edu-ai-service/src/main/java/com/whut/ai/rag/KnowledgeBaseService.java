package com.whut.ai.rag;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * 知识库构建服务
 *
 * 什么是知识库？
 * 知识库（Knowledge Base）就是把企业的文档、资料、知识等结构化地存储起来，
 * 让 AI 能够快速检索和利用。在 RAG（检索增强生成）架构中，
 * 知识库是"检索"那一步的数据来源。
 *
 * 构建流程总览
 * ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────────┐
 * │ 文档解析    │ → │ 文本分块    │ → │ 向量化      │ → │ 向量库存储   │
 * │ (Parser)    │ │ (Splitter)  │ │ (Embedding) │ │ (VectorStore)│
 * └─────────────┘ └─────────────┘ └─────────────┘ └──────────────┘
 *
 * 1. 文档解析：把 PDF/Word/TXT 等格式的文档变成纯文本
 * 2. 文本分块：把长文本切成合适大小的块（Chunk）
 * 3. 向量化：把每个文本块变成向量（Embedding）—— 调用 AI 模型
 * 4. 存储：把向量和原文一起存入 Chroma 向量数据库
 */
@Service
@ConditionalOnBean(VectorStore.class)
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    /**
     * 文档解析服务
     */
    private final DocumentParserService documentParserService;

    /**
     * 文本分块服务
     */
    private final TextSplitterService textSplitterService;

    /**
     * 向量存储（Spring AI 提供的抽象接口，底层是 Chroma）
     * Spring AI 的 VectorStore 是一个统一的向量存储抽象，
     * 支持 Milvus、Chroma、PGVector、Redis 等多种后端。
     * 我们配置了 Chroma，所以这里注入的就是 ChromaVectorStore。
     */
    private final VectorStore vectorStore;

    // 构造函数注入
    public KnowledgeBaseService(DocumentParserService documentParserService,
                                 TextSplitterService textSplitterService,
                                 VectorStore vectorStore) {
        this.documentParserService = documentParserService;
        this.textSplitterService = textSplitterService;
        this.vectorStore = vectorStore;
    }

    /**
     * 构建知识库
     * 这是知识库构建的主入口方法。
     * 根据请求参数，处理指定的文档或目录，将内容向量化后存入 Chroma。
     *
     * @param request 构建请求参数
     * @return 构建结果（成功处理的文档数和块数）
     * @throws Exception 构建过程中出现的异常
     */
    public BuildResult buildKnowledgeBase(KnowledgeBaseBuildRequest request) throws Exception {
        log.info("========== 开始构建知识库 ==========");
        log.info("文档路径: {}", request.getDocumentPath());
        log.info("分块大小: {} | 重叠大小: {}", request.getChunkSize(), request.getChunkOverlap());

        String documentPath = request.getDocumentPath();
        int chunkSize = request.getChunkSize();
        int chunkOverlap = request.getChunkOverlap();

        if (documentPath == null || documentPath.isBlank()) {
            throw new IllegalArgumentException("文档路径不能为空");
        }

        File pathFile = new File(documentPath);
        if (!pathFile.exists()) {
            throw new IllegalArgumentException("路径不存在: " + documentPath);
        }

        // ========== 第一步：收集所有文档块 ==========
        List<DocumentChunk> allChunks = new ArrayList<>();

        if (pathFile.isDirectory()) {
            // 是目录，批量解析
            log.info("检测到目录，开始批量处理...");
            List<DocumentParserService.ParsedDocument> docs =
                    documentParserService.parseDirectory(documentPath);

            for (DocumentParserService.ParsedDocument doc : docs) {
                List<DocumentChunk> chunks = textSplitterService.splitText(
                        doc.content(), chunkSize, chunkOverlap, doc.fileName());
                allChunks.addAll(chunks);
            }
        } else {
            // 是单个文件
            log.info("检测到单个文件，开始处理...");
            String content = documentParserService.parseDocument(documentPath);
            String fileName = pathFile.getName();
            List<DocumentChunk> chunks = textSplitterService.splitText(
                    content, chunkSize, chunkOverlap, fileName);
            allChunks.addAll(chunks);
        }

        log.info("文档解析和分块完成 | 共得到 {} 个文本块", allChunks.size());

        if (allChunks.isEmpty()) {
            throw new IllegalStateException("没有提取到任何文本块，请检查文档内容");
        }

        // ========== 第二步：转换为 Spring AI 的 Document 对象 ==========
        // Spring AI 的 VectorStore 接受的是它自己的 Document 类型
        List<Document> documents = convertToSpringAiDocuments(allChunks);
        log.info("准备向量化并存入 Chroma | 文档数: {}", documents.size());

        // ========== 第三步：向量化并写入 Chroma ==========
        // 这一步会自动调用 Embedding 模型生成向量，然后存入 Chroma
        // VectorStore.add() 方法内部做了两件事：
        // 1. 调用 EmbeddingModel 把文本变成向量
        // 2. 把向量和元数据写入 Chroma
        try {
            vectorStore.add(documents);
            log.info("向量写入 Chroma 成功！共写入 {} 条记录", documents.size());
        } catch (Exception e) {
            log.error("写入 Chroma 失败", e);
            throw new RuntimeException("写入向量数据库失败: " + e.getMessage(), e);
        }

        log.info("========== 知识库构建完成 ==========");

        return new BuildResult(
                allChunks.size(),
                (int) allChunks.stream().map(DocumentChunk::getSourceDocument).distinct().count(),
                documentPath
        );
    }

    /**
     * 检索相似文档
     * 根据用户的问题，在知识库中找到最相关的几个文本块。
     * 这是 RAG 中"检索"那一步的核心方法。
     *
     * 相似度检索的原理
     * 1. 把用户的问题也变成向量（Embedding）
     * 2. 在 Chroma 中计算这个向量和所有文档向量的相似度
     * 3. 返回最相似的前 K 个文档
     *
     * @param query 用户的问题
     * @param topK  返回最相似的前 K 个结果
     * @return 相似的文档块列表
     */
    public List<DocumentChunk> searchSimilar(String query, int topK) {
        log.info("开始相似性检索 | 查询: {} | TopK: {}", query, topK);

        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        if (topK <= 0) {
            topK = 4; // 默认返回前 4 个
        }

        // 调用 Spring AI 的 VectorStore 进行相似度检索
        // 使用 SearchRequest 构建检索请求
        // similaritySearch 内部会：
        // 1. 把 query 向量化
        // 2. 在 Chroma 中做相似性搜索
        // 3. 返回最相似的文档
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        List<Document> results = vectorStore.similaritySearch(request);

        // 转换为我们自己的 DocumentChunk 格式
        List<DocumentChunk> chunks = new ArrayList<>();
        for (Document doc : results) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(doc.getId());
            chunk.setContent(doc.getText());
            chunk.setMetadata(new HashMap<>(doc.getMetadata()));

            // 从元数据中提取来源和块索引
            Object source = doc.getMetadata().get("source");
            if (source != null) {
                chunk.setSourceDocument(source.toString());
            }
            Object idx = doc.getMetadata().get("chunk_index");
            if (idx != null) {
                chunk.setChunkIndex(Integer.parseInt(idx.toString()));
            }

            chunks.add(chunk);
        }

        log.info("相似性检索完成 | 返回 {} 条结果", chunks.size());
        return chunks;
    }

    /**
     * 将自定义 DocumentChunk 转换为 Spring AI 的 Document
     * Spring AI 的 VectorStore 接口使用它自己的 Document 类，
     * 所以我们需要把自己的数据结构转换过去。
     *
     * 为什么需要元数据（Metadata）？
     * 向量数据库里存的不只是向量，还会存原文和一些附加信息（元数据）。
     * 元数据的作用：
     * - 检索后可以知道这段内容来自哪篇文档
     * - 可以按来源、分类等条件过滤检索结果
     * - 方便溯源和调试
     */
    private List<Document> convertToSpringAiDocuments(List<DocumentChunk> chunks) {
        List<Document> documents = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            // 构建元数据 Map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", chunk.getSourceDocument());
            metadata.put("chunk_index", chunk.getChunkIndex());
            metadata.put("content_length", chunk.getContent().length());

            // 如果 chunk 自己有 metadata，也合并进去
            if (chunk.getMetadata() != null) {
                metadata.putAll(chunk.getMetadata());
            }

            // 创建 Spring AI 的 Document 对象
            // 注意：Spring AI 的 Document 会自动生成唯一 ID
            Document doc = new Document(chunk.getContent(), metadata);
            documents.add(doc);
        }

        return documents;
    }

    /**
     * 构建结果记录
     * 封装知识库构建的结果信息，用于返回给前端。
     *
     * @param totalChunks    总共处理的文本块数量
     * @param totalDocuments 总共处理的文档数量
     * @param sourcePath     处理的源路径
     */
    public record BuildResult(
            int totalChunks,
            int totalDocuments,
            String sourcePath
    ) {
    }
}
