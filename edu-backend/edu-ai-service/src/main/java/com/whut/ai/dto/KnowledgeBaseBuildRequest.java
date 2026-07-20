package com.whut.ai.dto;

public class KnowledgeBaseBuildRequest {

    private String documentPath;
    private Long kbId;
    private int chunkSize = 500;
    private int chunkOverlap = 50;

    public KnowledgeBaseBuildRequest() {
    }

    public KnowledgeBaseBuildRequest(String documentPath, int chunkSize, int chunkOverlap) {
        this.documentPath = documentPath;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }
}
