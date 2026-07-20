package com.whut.ai.dto;

public class ExamGenerateRequest {
    private Long courseId;
    private String courseName;
    private Integer count;
    private String knowledgeContext;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public String getKnowledgeContext() { return knowledgeContext; }
    public void setKnowledgeContext(String knowledgeContext) { this.knowledgeContext = knowledgeContext; }
}
