package com.whut.assessment.entity;

import lombok.Data;

@Data
public class CourseSnapshot {

    private Long id;
    private Long teacherId;
    private String name;
    private Integer status;
    private Integer deleted;
}
