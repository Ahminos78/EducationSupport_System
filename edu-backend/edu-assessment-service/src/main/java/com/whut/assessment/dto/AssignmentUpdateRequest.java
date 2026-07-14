package com.whut.assessment.dto;

import java.time.LocalDateTime;

public class AssignmentUpdateRequest {

    private String title;
    private String description;
    private Integer fullScore;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Boolean allowLateSubmission;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFullScore() {
        return fullScore;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setFullScore(Integer fullScore) {
        this.fullScore = fullScore;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Boolean getAllowLateSubmission() {
        return allowLateSubmission;
    }

    public void setAllowLateSubmission(Boolean allowLateSubmission) {
        this.allowLateSubmission = allowLateSubmission;
    }
}
