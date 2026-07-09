package com.whut.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Exam {

    private Long id;
    private Long courseId;
    private Long teacherId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer fullScore;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
