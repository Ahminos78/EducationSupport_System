package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_submission")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private String attachmentUrl;
    private Integer score;
    private String teacherComment;
    private String aiComment;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
