package com.whut.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edu.ai")
public class AiProperties {

    /** 默认使用的模型名称 */
    private String defaultModel = "deepseek-chat";

    /** 默认系统提示词 */
    private String systemPrompt = "你是一个在线教育平台的AI教学助手，请用中文回答用户的问题。";

    /** 向量存储配置 */
    private VectorStore vectorStore = new VectorStore();

    /** RAG 配置 */
    private Rag rag = new Rag();

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public VectorStore getVectorStore() {
        return vectorStore;
    }

    public void setVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public static class VectorStore {
        private double similarityThreshold = 0.7;
        private int maxResults = 5;

        public double getSimilarityThreshold() { return similarityThreshold; }
        public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
        public int getMaxResults() { return maxResults; }
        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    }

    public static class Rag {
        private boolean enabled = false;
        private String documentDir = "./data/documents";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDocumentDir() { return documentDir; }
        public void setDocumentDir(String documentDir) { this.documentDir = documentDir; }
    }
}
