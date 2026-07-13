package com.whut.assessment.dto;

import lombok.Data;

@Data
public class ExamAttemptGradeRequest {

    private Integer score;
    private String teacherComment;
}
