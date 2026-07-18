package com.whut.ai.service;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final DocumentParserService documentParserService;
    private final TextSplitterService textSplitterService;
    private final VectorStore vectorStore;

    public KnowledgeBaseService(DocumentParserService documentParserService,
                                TextSplitterService textSplitterService,
                                VectorStore vectorStore) {
        this.documentParserService = documentParserService;
        this.textSplitterService = textSplitterService;
        this.vectorStore = vectorStore;
    }

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

        List<DocumentChunk> allChunks = new ArrayList<>();

        if (pathFile.isDirectory()) {
            log.info("检测到目录，开始批量解析...");
            List<DocumentParserService.ParsedDocument> docs =
                    documentParserService.parseDirectory(documentPath);
            for (DocumentParserService.ParsedDocument doc : docs) {
                allChunks.addAll(textSplitterService.splitText(doc.content(), chunkSize, chunkOverlap, doc.fileName()));
            }
        } else {
            log.info("检测到单个文件，开始解析...");
            String content = documentParserService.parseDocument(documentPath);
            allChunks.addAll(textSplitterService.splitText(content, chunkSize, chunkOverlap, pathFile.getName()));
        }

        log.info("文档解析和分块完成 | 共得到 {} 个文本块", allChunks.size());
        if (allChunks.isEmpty()) throw new IllegalStateException("没有提取到任何文本块，请检查文档内容");

        List<Document> documents = convertToSpringAiDocuments(allChunks);
        log.info("准备写入向量数据库 Chroma | 文档数: {}", documents.size());

        int batchSize = 10;
        int total = documents.size();
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = documents.subList(i, end);
            try {
                vectorStore.add(batch);
                log.info("写入批次成功 | {}/{}", end, total);
            } catch (Exception e) {
                log.error("写入批次失败 | {}/{}: {}", i, total, e.getMessage());
                throw new RuntimeException("写入向量数据库失败: " + e.getMessage(), e);
            }
        }
        log.info("写入 Chroma 成功，共写入 {} 条记录", total);

        log.info("========== 知识库构建完成 ==========");
        return new BuildResult(
                allChunks.size(),
                (int) allChunks.stream().map(DocumentChunk::getSourceDocument).distinct().count(),
                documentPath
        );
    }

    public List<DocumentChunk> searchSimilar(String query, int topK) {
        log.info("开始语义检索 | 查询: {} | TopK: {}", query, topK);
        if (query == null || query.isBlank()) return new ArrayList<>();
        if (topK <= 0) topK = 4;

        SearchRequest request = SearchRequest.builder().query(query).topK(topK).build();
        List<Document> results = vectorStore.similaritySearch(request);

        List<DocumentChunk> chunks = new ArrayList<>();
        for (Document doc : results) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(doc.getId());
            chunk.setContent(doc.getText());
            chunk.setMetadata(doc.getMetadata());
            Object source = doc.getMetadata().get("source");
            if (source != null) chunk.setSourceDocument(source.toString());
            Object idx = doc.getMetadata().get("chunk_index");
            if (idx != null) chunk.setChunkIndex(Integer.parseInt(idx.toString()));
            chunks.add(chunk);
        }

        log.info("语义检索完成 | 找到 {} 个结果", chunks.size());
        return chunks;
    }

    private List<Document> convertToSpringAiDocuments(List<DocumentChunk> chunks) {
        List<Document> documents = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", chunk.getSourceDocument());
            metadata.put("chunk_index", chunk.getChunkIndex());
            metadata.put("content_length", chunk.getContent().length());
            if (chunk.getMetadata() != null) metadata.putAll(chunk.getMetadata());
            documents.add(new Document(chunk.getContent(), metadata));
        }
        return documents;
    }

    public record BuildResult(int totalChunks, int totalDocuments, String sourcePath) {}
}
