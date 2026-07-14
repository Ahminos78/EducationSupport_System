package com.whut.assessment.dto;

import lombok.Data;

@Data
public class QuestionCreateRequest {

    private Integer type;
    private String title;
    private String options;
    private String answer;
    private Integer score;
    private Integer sortOrder;
}
