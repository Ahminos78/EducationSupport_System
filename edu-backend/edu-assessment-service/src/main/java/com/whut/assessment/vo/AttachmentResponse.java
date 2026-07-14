package com.whut.assessment.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentResponse {
    private Long id;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
    private String downloadUrl;
}
