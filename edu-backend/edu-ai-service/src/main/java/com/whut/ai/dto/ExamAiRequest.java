package com.whut.ai.dto;

public class ExamAiRequest {

    private String courseName;
    private String courseDescription;
    private int count;
    private String submissionContent;
    private String assignmentTitle;
    private Integer score;

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public String getSubmissionContent() { return submissionContent; }
    public void setSubmissionContent(String submissionContent) { this.submissionContent = submissionContent; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}