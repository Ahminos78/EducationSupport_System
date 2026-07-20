package com.whut.ai.vector;

import com.whut.ai.config.AiProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量存储服务。
 * 用于文档的向量化存储与语义检索，支撑 RAG 能力。
 * 默认使用 PGVector，也可切换为其他 VectorStore 实现。
 * 仅在存在 VectorStore Bean 时才加载，避免无向量数据库时启动失败。
 */
@Service
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final AiProperties aiProperties;

    public VectorStoreService(VectorStore vectorStore, AiProperties aiProperties) {
        this.vectorStore = vectorStore;
        this.aiProperties = aiProperties;
    }

    /**
     * 添加文档到向量库
     */
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }

    /**
     * 从向量库中删除文档
     */
    public void deleteDocuments(List<String> docIds) {
        vectorStore.delete(docIds);
    }

    /**
     * 语义检索相似文档
     */
    public List<Document> search(String query) {
        return search(query, aiProperties.getVectorStore().getMaxResults());
    }

    /**
     * 语义检索，指定返回数量
     */
    public List<Document> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(Math.min(topK, 50))
                .similarityThreshold(aiProperties.getVectorStore().getSimilarityThreshold())
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * 带元数据过滤的语义检索
     */
    public List<Document> search(String query, int topK, Map<String, String> filterExpressions) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(Math.min(topK, 50))
                .similarityThreshold(aiProperties.getVectorStore().getSimilarityThreshold())
                .filterExpression(buildFilterExpression(filterExpressions))
                .build();
        return vectorStore.similaritySearch(request);
    }

    private String buildFilterExpression(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        return filters.entrySet().stream()
                .map(e -> e.getKey() + " == '" + e.getValue() + "'")
                .collect(Collectors.joining(" && "));
    }
}
