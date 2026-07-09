package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_assignment")
public class Assignment {

    @TableId(type = IdType.AUTO)
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
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
