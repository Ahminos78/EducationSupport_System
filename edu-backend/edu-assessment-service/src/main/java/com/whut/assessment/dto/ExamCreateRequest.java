package com.whut.assessment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamCreateRequest {

    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer fullScore;
    private Integer duration;
    private Integer status;
}
