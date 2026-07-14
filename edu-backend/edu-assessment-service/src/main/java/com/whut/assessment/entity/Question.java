package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Integer type;
    private String title;
    private String options;
    private String answer;
    private Integer score;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
