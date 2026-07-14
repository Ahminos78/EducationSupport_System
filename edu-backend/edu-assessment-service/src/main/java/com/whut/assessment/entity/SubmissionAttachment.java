package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_submission_attachment")
public class SubmissionAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
