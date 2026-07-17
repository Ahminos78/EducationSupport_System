package com.whut.ai.dto;

import java.util.Map;

/**
 * 文本块（Chunk）数据传输对象
 * 知识库构建的核心数据结构，表示一篇文档被切分后的一个文本块。
 * 每个文本块会被向量化后存入 Chroma 向量数据库。
 *
 * 为什么需要 Chunk？
 * 大模型的上下文窗口（Context Window）是有限的，一篇几十页的企业文档
 * 不可能全部塞进 Prompt 里。所以需要把长文档切分成多个小块，
 * 检索时只返回最相关的几个块给大模型参考。
 *
 * 字段说明
 * - id：文档块的唯一标识，用于向量库中定位和删除
 * - content：文本块的实际内容，会被拿去做向量化
 * - metadata：元数据（如文件名、页码、块序号等），方便检索后溯源
 * - chunkIndex：该块在原文档中的序号（第几个块）
 * - sourceDocument：来源文档名称，用于标识这是哪篇文档的内容
 */
public class DocumentChunk {

    // 文档块的唯一 ID（由系统生成，用于向量库存储）
    private String id;

    // 文本块的内容（核心字段，会被 Embedding 模型向量化）
    private String content;

    // 元数据 Map，存储额外信息：
    // - source: 来源文件名
    // - chunk_index: 块序号
    // - page_number: 页码（PDF 文档有）
    // - category: 文档分类
    // - ... 其他自定义字段
    private Map<String, Object> metadata;

    // 该块在原文档中的索引（第几个块，从 0 开始）
    private int chunkIndex;

    // 来源文档名称（如 "公司介绍.pdf"）
    private String sourceDocument;

    // 无参构造函数（Jackson 序列化需要）
    public DocumentChunk() {
    }

    // 便捷构造函数
    public DocumentChunk(String content, int chunkIndex, String sourceDocument) {
        this.content = content;
        this.chunkIndex = chunkIndex;
        this.sourceDocument = sourceDocument;
    }

    // ========== Getter & Setter ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(String sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    @Override
    public String toString() {
        return "DocumentChunk{" +
                "id='" + id + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", sourceDocument='" + sourceDocument + '\'' +
                '}';
    }
}
