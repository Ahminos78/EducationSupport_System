package com.whut.ai.dto;

/**
 * 知识库构建请求 DTO
 * 前端触发知识库构建时发送的请求参数。
 *
 * 参数说明
 * - documentPath：要处理的文档路径（可以是单个文件或目录）
 * - chunkSize：每个文本块的大小（字符数），默认 500
 * - chunkOverlap：相邻块之间的重叠字符数，默认 50
 *
 * 什么是 Chunk Overlap？
 * 想象一下把一篇文章切成几段，如果切得太干脆，
 * 可能会把一个完整的句子或段落切成两半，导致语义断裂。
 * 重叠（Overlap）就是让相邻的两个块共享一部分内容，
 * 这样即使切分点正好在句子中间，其中一个块也能包含完整的句子。
 */
public class KnowledgeBaseBuildRequest {

    // 文档路径（可以是单个文件路径，也可以是目录路径）
    // 例如："D:/docs/company_intro.pdf" 或 "D:/docs/"
    private String documentPath;

    // 每个文本块的大小（字符数）
    // 建议值：
    // - 通用场景：500 - 1000 字符
    // - 详细问答：300 - 500 字符（更精准）
    // - 摘要类场景：1000 - 2000 字符（更多上下文）
    private int chunkSize = 500;

    // 相邻块之间的重叠字符数
    // 一般设为 chunkSize 的 10% - 20% 比较合适。
    // 重叠太多会增加冗余，太少又起不到保持上下文的作用。
    private int chunkOverlap = 50;

    // 无参构造函数
    public KnowledgeBaseBuildRequest() {
    }

    // 便捷构造函数
    public KnowledgeBaseBuildRequest(String documentPath, int chunkSize, int chunkOverlap) {
        this.documentPath = documentPath;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    // ========== Getter & Setter ==========

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
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
