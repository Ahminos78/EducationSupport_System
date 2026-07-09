package com.whut.enrollment.entity;

import lombok.Data;

@Data
public class CourseSnapshot {

    private Long id;
    private Long teacherId;
    private String name;
    private Integer maxStudents;
    private Integer enrolledCount;
    private Integer status;
    private Integer deleted;
}
