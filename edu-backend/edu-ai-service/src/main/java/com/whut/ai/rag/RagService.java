package com.whut.ai.rag;

import com.whut.ai.config.AiProperties;
import com.whut.ai.dto.RagQueryRequest;
import com.whut.ai.vector.VectorStoreService;
import com.whut.ai.vo.RagQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RAG（检索增强生成）服务。
 * 负责从向量库检索相关知识并组装上下文，用于增强 AI 回答。
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStoreService vectorStoreService;
    private final AiProperties aiProperties;

    public RagService(VectorStoreService vectorStoreService, AiProperties aiProperties) {
        this.vectorStoreService = vectorStoreService;
        this.aiProperties = aiProperties;
    }

    /**
     * 根据查询检索相关知识文档
     */
    public RagQueryResponse search(RagQueryRequest request) {
        String query = request.getQuery();
        int topK = request.getTopK() != null ? request.getTopK() : aiProperties.getVectorStore().getMaxResults();
        List<Document> documents;

        if (request.getCourseId() != null) {
            documents = vectorStoreService.search(query, topK, Map.of("courseId", String.valueOf(request.getCourseId())));
        } else {
            documents = vectorStoreService.search(query, topK);
        }

        RagQueryResponse response = new RagQueryResponse();
        response.setTotal(documents.size());
        response.setChunks(documents.stream().map(this::toChunk).toList());

        log.info("RAG 检索完成: query={}, results={}", query, documents.size());
        return response;
    }

    /**
     * 检索并组装上下文文本（供 ChatService 使用）
     */
    public String retrieveContext(String query, Long courseId) {
        int topK = aiProperties.getVectorStore().getMaxResults();
        List<Document> documents;

        if (courseId != null) {
            documents = vectorStoreService.search(query, topK, Map.of("courseId", String.valueOf(courseId)));
        } else {
            documents = vectorStoreService.search(query, topK);
        }

        if (documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n【参考知识】\n");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(i + 1).append(". ");
            if (doc.getMetadata() != null && doc.getMetadata().get("title") != null) {
                context.append("[").append(doc.getMetadata().get("title")).append("] ");
            }
            context.append(doc.getText()).append("\n");
        }
        return context.toString();
    }

    private RagQueryResponse.DocumentChunk toChunk(Document doc) {
        RagQueryResponse.DocumentChunk chunk = new RagQueryResponse.DocumentChunk();
        chunk.setId(doc.getId());
        if (doc.getMetadata() != null) {
            chunk.setTitle((String) doc.getMetadata().getOrDefault("title", ""));
            chunk.setCourseId(doc.getMetadata().get("courseId") != null
                    ? Long.valueOf(doc.getMetadata().get("courseId").toString()) : null);
            chunk.setTags((String) doc.getMetadata().getOrDefault("tags", ""));
            chunk.setDocType((String) doc.getMetadata().getOrDefault("docType", ""));
        }
        chunk.setContent(doc.getText());
        chunk.setScore(doc.getScore());
        return chunk;
    }
}
