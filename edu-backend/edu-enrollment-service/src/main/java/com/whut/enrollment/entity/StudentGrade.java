package com.whut.enrollment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("tb_student_grade")
public class StudentGrade {
    private Long id;
    private Long enrollmentId;
    private Long componentId;
    private BigDecimal score;
}
