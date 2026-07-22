package com.whut.ai.rag;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import com.whut.ai.entity.KbDocument;
import com.whut.ai.entity.KbKnowledgeBase;
import com.whut.ai.mapper.KbDocumentMapper;
import com.whut.ai.mapper.KbKnowledgeBaseMapper;
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
    private final KbKnowledgeBaseMapper kbMapper;
    private final KbDocumentMapper docMapper;

    public KnowledgeBaseService(DocumentParserService documentParserService,
                                 TextSplitterService textSplitterService,
                                 VectorStore vectorStore,
                                 KbKnowledgeBaseMapper kbMapper,
                                 KbDocumentMapper docMapper) {
        this.documentParserService = documentParserService;
        this.textSplitterService = textSplitterService;
        this.vectorStore = vectorStore;
        this.kbMapper = kbMapper;
        this.docMapper = docMapper;
    }

    public BuildResult buildKnowledgeBase(KnowledgeBaseBuildRequest request) throws Exception {
        Long kbId = request.getKbId();
        String documentPath = request.getDocumentPath();
        int chunkSize = request.getChunkSize();
        int chunkOverlap = request.getChunkOverlap();

        if (documentPath == null || documentPath.isBlank()) {
            throw new IllegalArgumentException("文档路径不能为空");
        }
        if (kbId == null) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }

        KbKnowledgeBase kb = kbMapper.selectById(kbId);
        if (kb == null) {
            throw new IllegalArgumentException("知识库不存在: " + kbId);
        }

        File pathFile = new File(documentPath);
        if (!pathFile.exists()) {
            throw new IllegalArgumentException("路径不存在: " + documentPath);
        }

        log.info("========== 开始构建知识库 ==========");
        log.info("知识库: {} (id={}) | 路径: {} | 分块: {}/{}", kb.getName(), kbId, documentPath, chunkSize, chunkOverlap);

        // 收集所有文件路径
        List<File> docFiles = new ArrayList<>();
        if (pathFile.isDirectory()) {
            File[] files = pathFile.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".pdf") || lower.endsWith(".txt") || lower.endsWith(".docx")
                        || lower.endsWith(".md") || lower.endsWith(".markdown");
            });
            if (files != null) {
                for (File f : files) {
                    docFiles.add(f);
                }
            }
        } else {
            docFiles.add(pathFile);
        }

        if (docFiles.isEmpty()) {
            throw new IllegalStateException("没有找到可解析的文档");
        }

        log.info("共发现 {} 个文档", docFiles.size());

        // 逐文档处理，避免内存溢出
        int totalChunks = 0;
        for (File file : docFiles) {
            try {
                String content = documentParserService.parseDocument(file.getAbsolutePath());
                List<DocumentChunk> chunks = textSplitterService.splitText(content, chunkSize, chunkOverlap, file.getName());

                if (chunks.isEmpty()) {
                    log.warn("文档无有效内容，跳过 | {}", file.getName());
                    continue;
                }

                // 写入 MySQL 文档记录
                KbDocument doc = new KbDocument();
                doc.setKbId(kbId);
                doc.setFileName(file.getName());
                doc.setOriginalName(file.getName());
                doc.setFileSize(file.length());
                doc.setFileType(getFileExtension(file.getName()));
                doc.setChunkCount(chunks.size());
                doc.setStatus(0);
                docMapper.insert(doc);

                // 补充 kb_id、doc_id、courseId 元数据
                for (DocumentChunk chunk : chunks) {
                    Map<String, Object> meta = chunk.getMetadata() != null ? chunk.getMetadata() : new HashMap<>();
                    meta.put("kb_id", kbId);
                    meta.put("doc_id", doc.getId());
                    if (request.getCourseId() != null) {
                        meta.put("courseId", String.valueOf(request.getCourseId()));
                    }
                    chunk.setMetadata(meta);
                }

                // 写入 Chroma
                List<Document> documents = convertToSpringAiDocuments(chunks);
                vectorStore.add(documents);

                // 更新文档状态为成功
                doc.setStatus(1);
                docMapper.updateById(doc);

                totalChunks += chunks.size();
                log.info("文档处理完成 | {} | {} 个块 | docId={}", file.getName(), chunks.size(), doc.getId());

                // 释放内存引用
                content = null;
                chunks.clear();
                documents.clear();

            } catch (Exception e) {
                log.error("处理文档失败: {} | {}", file.getName(), e.getMessage());
                // 更新文档状态为失败
                List<KbDocument> docs = docMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                                .eq(KbDocument::getKbId, kbId)
                                .eq(KbDocument::getFileName, file.getName())
                );
                for (KbDocument d : docs) {
                    d.setStatus(2);
                    d.setErrorMsg(e.getMessage());
                    docMapper.updateById(d);
                }
                // 继续处理下一个文档，不中断
            }
        }

        log.info("========== 知识库构建完成 | 总块数: {} ==========", totalChunks);

        return new BuildResult(totalChunks, docFiles.size(), documentPath);
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    public List<DocumentChunk> searchSimilar(String query, int topK) {
        log.info("开始相似性检索 | 查询: {} | TopK: {}", query, topK);

        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        if (topK <= 0) {
            topK = 4;
        }

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        List<Document> results = vectorStore.similaritySearch(request);

        List<DocumentChunk> chunks = new ArrayList<>();
        for (Document doc : results) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(doc.getId());
            chunk.setContent(doc.getText());
            chunk.setMetadata(new HashMap<>(doc.getMetadata()));

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

    private List<Document> convertToSpringAiDocuments(List<DocumentChunk> chunks) {
        List<Document> documents = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", chunk.getSourceDocument());
            metadata.put("chunk_index", chunk.getChunkIndex());
            metadata.put("content_length", chunk.getContent().length());

            if (chunk.getMetadata() != null) {
                metadata.putAll(chunk.getMetadata());
            }

            Document doc = new Document(chunk.getContent(), metadata);
            documents.add(doc);
        }

        return documents;
    }

    public record BuildResult(
            int totalChunks,
            int totalDocuments,
            String sourcePath
    ) {
    }
}
