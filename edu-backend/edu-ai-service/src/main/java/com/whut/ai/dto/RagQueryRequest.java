package com.whut.ai.dto;

public class RagQueryRequest {

    /** 查询文本 */
    private String query;

    /** 关联课程 ID（限定检索范围） */
    private Long courseId;

    /** 返回结果数量 */
    private Integer topK;

    /** 相似度阈值 */
    private Double similarityThreshold;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(Double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}
