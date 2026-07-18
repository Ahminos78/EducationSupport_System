package com.whut.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_course_class")
public class CourseClass {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long teacherId;
    private String name;
    private Integer maxStudents;
    private Integer enrolledCount;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
