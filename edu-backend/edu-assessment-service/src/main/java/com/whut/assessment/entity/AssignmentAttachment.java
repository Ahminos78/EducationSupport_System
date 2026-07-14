package com.whut.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_assignment_attachment")
public class AssignmentAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long fileSize;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}
