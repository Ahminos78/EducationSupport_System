package com.whut.ai.vo;

import java.util.List;

public class ChatResponse {

    /** 回答内容 */
    private String content;

    /** 会话 ID */
    private String sessionId;

    /** 使用的模型 */
    private String model;

    /** Token 用量 */
    private TokenUsage tokenUsage;

    /** 引用的知识来源 */
    private List<ReferenceSource> sources;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(TokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public List<ReferenceSource> getSources() {
        return sources;
    }

    public void setSources(List<ReferenceSource> sources) {
        this.sources = sources;
    }

    public static class TokenUsage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;

        public int getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }

    public static class ReferenceSource {
        private String title;
        private String snippet;
        private Double score;
        private String source;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSnippet() {
            return snippet;
        }

        public void setSnippet(String snippet) {
            this.snippet = snippet;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
