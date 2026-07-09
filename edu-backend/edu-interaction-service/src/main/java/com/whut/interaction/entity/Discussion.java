package com.whut.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_discussion")
public class Discussion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long parentId;
    private Long authorId;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
