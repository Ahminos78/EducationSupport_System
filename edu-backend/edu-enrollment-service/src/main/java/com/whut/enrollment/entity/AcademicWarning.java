package com.whut.enrollment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_academic_warning")
public class AcademicWarning {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long enrollmentId;
    private Integer warningType;
    private Integer severity;
    private BigDecimal currentScore;
    private BigDecimal thresholdScore;
    private String description;
    private Integer status;
    private Long processedBy;
    private String processComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
}
