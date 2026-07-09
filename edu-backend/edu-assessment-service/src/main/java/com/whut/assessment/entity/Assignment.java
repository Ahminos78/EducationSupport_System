package com.whut.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Assignment {

    private Long id;
    private Long courseId;
    private Long teacherId;
    private String title;
    private String description;
    private Integer fullScore;
    private LocalDateTime deadline;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
