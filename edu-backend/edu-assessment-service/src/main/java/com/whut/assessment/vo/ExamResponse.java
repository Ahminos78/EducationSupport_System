package com.whut.assessment.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer fullScore;
    private Integer status;
    private LocalDateTime createdAt;
}
