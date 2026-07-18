package com.whut.ai.dto;

public class KnowledgeBaseBuildRequest {

    private String documentPath;
    private int chunkSize = 500;
    private int chunkOverlap = 50;
    private String collectionName;

    public KnowledgeBaseBuildRequest() {
    }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
    public int getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
}
