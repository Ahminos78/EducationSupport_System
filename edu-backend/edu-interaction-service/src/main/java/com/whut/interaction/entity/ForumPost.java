package com.whut.interaction.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPost {

    private Long id;
    private Long courseId;
    private Long authorId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
