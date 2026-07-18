package com.whut.enrollment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("tb_grade_component")
public class GradeComponent {
    private Long id;
    private Long courseId;
    private String name;
    private BigDecimal weight;
    private Integer maxScore;
    private Integer sortOrder;
}
