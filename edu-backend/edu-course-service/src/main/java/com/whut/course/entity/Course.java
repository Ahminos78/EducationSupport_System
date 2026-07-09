package com.whut.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String name;
    private String description;
    private String coverUrl;
    private Integer maxStudents;
    private Integer enrolledCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
