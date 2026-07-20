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

        List<DocumentChunk> allChunks = new ArrayList<>();
        List<File> docFiles = new ArrayList<>();

        if (pathFile.isDirectory()) {
            List<DocumentParserService.ParsedDocument> docs = documentParserService.parseDirectory(documentPath);
            for (DocumentParserService.ParsedDocument doc : docs) {
                List<DocumentChunk> chunks = textSplitterService.splitText(doc.content(), chunkSize, chunkOverlap, doc.fileName());
                allChunks.addAll(chunks);
                docFiles.add(new File(pathFile, doc.fileName()));
            }
        } else {
            String content = documentParserService.parseDocument(documentPath);
            String fileName = pathFile.getName();
            List<DocumentChunk> chunks = textSplitterService.splitText(content, chunkSize, chunkOverlap, fileName);
            allChunks.addAll(chunks);
            docFiles.add(pathFile);
        }

        log.info("文档解析和分块完成 | 共 {} 个文件, {} 个文本块", docFiles.size(), allChunks.size());

        if (allChunks.isEmpty()) {
            throw new IllegalStateException("没有提取到任何文本块，请检查文档内容");
        }

        // 按源文档分组统计 chunk 数
        Map<String, Integer> chunkCountByFile = new LinkedHashMap<>();
        for (DocumentChunk chunk : allChunks) {
            chunkCountByFile.merge(chunk.getSourceDocument(), 1, Integer::sum);
        }

        // 逐文件写入 kb_document 记录
        for (File file : docFiles) {
            String fileName = file.getName();
            int chunkCount = chunkCountByFile.getOrDefault(fileName, 0);

            KbDocument doc = new KbDocument();
            doc.setKbId(kbId);
            doc.setFileName(fileName);
            doc.setOriginalName(fileName);
            doc.setFileSize(file.length());
            doc.setFileType(getFileExtension(fileName));
            doc.setChunkCount(chunkCount);
            doc.setStatus(0);
            docMapper.insert(doc);

            log.info("MySQL 文档记录已创建 | docId={} | 文件: {} | 块数: {}", doc.getId(), fileName, chunkCount);

            // 给属于该文档的 chunk 补充 kb_id 和 doc_id 元数据
            for (DocumentChunk chunk : allChunks) {
                if (fileName.equals(chunk.getSourceDocument())) {
                    Map<String, Object> meta = chunk.getMetadata() != null ? chunk.getMetadata() : new HashMap<>();
                    meta.put("kb_id", kbId);
                    meta.put("doc_id", doc.getId());
                    chunk.setMetadata(meta);
                }
            }
        }

        // 转换为 Spring AI Document 并写入 Chroma
        List<Document> documents = convertToSpringAiDocuments(allChunks);
        try {
            vectorStore.add(documents);
            log.info("向量写入 Chroma 成功！共写入 {} 条记录", documents.size());
        } catch (Exception e) {
            log.error("写入 Chroma 失败", e);
            throw new RuntimeException("写入向量数据库失败: " + e.getMessage(), e);
        }

        // 更新所有文档状态为成功
        for (File file : docFiles) {
            updateDocStatus(kbId, file.getName(), 1, null);
        }

        log.info("========== 知识库构建完成 ==========");

        return new BuildResult(
                allChunks.size(),
                docFiles.size(),
                documentPath
        );
    }

    private void updateDocStatus(Long kbId, String fileName, int status, String errorMsg) {
        List<KbDocument> docs = docMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
                        .eq(KbDocument::getFileName, fileName)
        );
        for (KbDocument doc : docs) {
            doc.setStatus(status);
            doc.setErrorMsg(errorMsg);
            docMapper.updateById(doc);
        }
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
