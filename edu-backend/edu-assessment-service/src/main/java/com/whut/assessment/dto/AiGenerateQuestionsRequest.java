package com.whut.assessment.dto;

public class AiGenerateQuestionsRequest {

    private String courseName;
    private String courseDescription;
    private int count;

    public AiGenerateQuestionsRequest() {}

    public AiGenerateQuestionsRequest(String courseName, String courseDescription, int count) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.count = count;
    }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}