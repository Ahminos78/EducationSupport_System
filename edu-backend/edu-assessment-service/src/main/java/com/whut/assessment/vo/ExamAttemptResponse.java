package com.whut.assessment.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamAttemptResponse {

    private Long id;
    private Long examId;
    private String examTitle;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private String answerContent;
    private Integer status;
    private Integer score;
    private String teacherComment;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}
