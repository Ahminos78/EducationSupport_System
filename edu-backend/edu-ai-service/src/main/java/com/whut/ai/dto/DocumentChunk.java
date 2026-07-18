package com.whut.ai.dto;

import java.util.Map;

public class DocumentChunk {

    private String id;
    private String content;
    private Map<String, Object> metadata;
    private int chunkIndex;
    private String sourceDocument;

    public DocumentChunk() {
    }

    public DocumentChunk(String content, int chunkIndex, String sourceDocument) {
        this.content = content;
        this.chunkIndex = chunkIndex;
        this.sourceDocument = sourceDocument;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getSourceDocument() { return sourceDocument; }
    public void setSourceDocument(String sourceDocument) { this.sourceDocument = sourceDocument; }
}
