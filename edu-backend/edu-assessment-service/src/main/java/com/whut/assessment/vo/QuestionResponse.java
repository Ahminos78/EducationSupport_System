package com.whut.assessment.vo;

import lombok.Data;

@Data
public class QuestionResponse {

    private Long id;
    private Long examId;
    private Integer type;
    private String title;
    private String options;
    private String answer;
    private Integer score;
    private Integer sortOrder;
}
