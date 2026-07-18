package com.whut.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_course_schedule")
public class CourseSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private Integer startWeek;
    private Integer endWeek;
    private Integer weekType;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
