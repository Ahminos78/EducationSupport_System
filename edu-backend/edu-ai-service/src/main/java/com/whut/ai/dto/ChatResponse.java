package com.whut.ai.dto;

import java.util.List;

public class ChatResponse {

    private boolean success;
    private String answer;
    private List<Source> sources;
    private String model;

    public ChatResponse() {
    }

    public static ChatResponse ok(String answer, List<Source> sources, String model) {
        ChatResponse resp = new ChatResponse();
        resp.success = true;
        resp.answer = answer;
        resp.sources = sources;
        resp.model = model;
        return resp;
    }

    public static ChatResponse fail(String message) {
        ChatResponse resp = new ChatResponse();
        resp.success = false;
        resp.answer = message;
        return resp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<Source> getSources() { return sources; }
    public void setSources(List<Source> sources) { this.sources = sources; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public static class Source {
        private String content;
        private String document;
        private double score;

        public Source() {}

        public Source(String content, String document, double score) {
            this.content = content;
            this.document = document;
            this.score = score;
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDocument() { return document; }
        public void setDocument(String document) { this.document = document; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
