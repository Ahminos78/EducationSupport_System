package com.whut.assessment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamWithQuestionsRequest {

    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer fullScore;
    private String type;
    private Integer duration;
    private Integer status;
    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {
        private Integer type;
        private String title;
        private String options;
        private String answer;
        private Integer score;
        private Integer sortOrder;
    }
}
