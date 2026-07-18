package com.whut.ai.dto;

public class ChatRequest {

    private String question;
    private int topK = 4;
    private String collectionName;

    public ChatRequest() {
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
}
