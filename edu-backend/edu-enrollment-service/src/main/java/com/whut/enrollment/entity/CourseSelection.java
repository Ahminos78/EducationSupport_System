package com.whut.enrollment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelection {

    private Long id;
    private Long courseId;
    private Long studentId;
    private Integer status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
