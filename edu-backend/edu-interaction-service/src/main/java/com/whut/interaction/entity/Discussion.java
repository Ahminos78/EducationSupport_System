package com.whut.interaction.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Discussion {

    private Long id;
    private Long courseId;
    private Long parentId;
    private Long authorId;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
