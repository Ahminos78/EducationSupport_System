package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_exam_attempt")
public class ExamAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private String answerContent;
    private Integer status;
    private Integer score;
    private String teacherComment;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
