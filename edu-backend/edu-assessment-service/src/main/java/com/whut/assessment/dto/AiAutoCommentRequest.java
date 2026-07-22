package com.whut.assessment.dto;

public class AiAutoCommentRequest {

    private String assignmentTitle;
    private String submissionContent;
    private Integer score;

    public AiAutoCommentRequest() {}

    public AiAutoCommentRequest(String assignmentTitle, String submissionContent, Integer score) {
        this.assignmentTitle = assignmentTitle;
        this.submissionContent = submissionContent;
        this.score = score;
    }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public String getSubmissionContent() { return submissionContent; }
    public void setSubmissionContent(String submissionContent) { this.submissionContent = submissionContent; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}