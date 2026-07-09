package com.whut.interaction.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumReply {

    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
