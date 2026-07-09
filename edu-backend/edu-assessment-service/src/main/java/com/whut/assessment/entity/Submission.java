package com.whut.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Submission {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private String attachmentUrl;
    private Integer score;
    private String teacherComment;
    private String aiComment;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
