package com.whut.ai.rag;

import com.whut.ai.dto.DocumentUploadRequest;
import com.whut.ai.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文档管理服务。
 * 负责文档的上传、解析、切片和向量化存储。
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final VectorStoreService vectorStoreService;

    @Value("${edu.ai.rag.document-dir:./data/documents}")
    private String documentDir;

    public DocumentService(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * 上传文档并向量化
     */
    public String uploadAndIndex(DocumentUploadRequest request) {
        try {
            // 保存文件
            String fileName = saveFile(request);
            // 解析文档
            List<Document> documents = parseDocument(fileName, request);
            // 切片
            List<Document> chunks = new TokenTextSplitter().split(documents);
            // 注入元数据
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("title", request.getTitle());
                if (request.getCourseId() != null) {
                    chunk.getMetadata().put("courseId", request.getCourseId());
                }
                if (request.getTags() != null) {
                    chunk.getMetadata().put("tags", request.getTags());
                }
                if (request.getDocType() != null) {
                    chunk.getMetadata().put("docType", request.getDocType());
                }
            });
            // 向量化存储
            vectorStoreService.addDocuments(chunks);
            log.info("文档向量化完成: title={}, chunks={}", request.getTitle(), chunks.size());
            return "ok";
        } catch (Exception e) {
            log.error("文档上传与向量化失败: title={}", request.getTitle(), e);
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }

    private String saveFile(DocumentUploadRequest request) throws IOException {
        Path dir = Path.of(documentDir);
        Files.createDirectories(dir);

        String fileName = UUID.randomUUID() + "_" + request.getTitle();
        Path filePath = dir.resolve(fileName);

        if (request.getContent() != null) {
            byte[] content;
            if (request.getContent().startsWith("text/")) {
                content = request.getContent().getBytes();
            } else {
                content = Base64.getDecoder().decode(request.getContent());
            }
            Files.write(filePath, content);
        } else if (request.getFileUrl() != null) {
            // 实际项目中可从 URL 下载文件
            throw new UnsupportedOperationException("从 URL 下载文件暂未实现");
        }

        return filePath.toString();
    }

    private List<Document> parseDocument(String filePath, DocumentUploadRequest request) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + filePath);
        }

        Resource resource = new FileSystemResource(file);
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf") || fileName.endsWith(".docx")
                || fileName.endsWith(".pptx") || fileName.endsWith(".txt")) {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            return reader.read();
        }

        // 纯文本内容处理
        Document doc = new Document(request.getContent(), Map.of("title", request.getTitle()));
        return List.of(doc);
    }
}
