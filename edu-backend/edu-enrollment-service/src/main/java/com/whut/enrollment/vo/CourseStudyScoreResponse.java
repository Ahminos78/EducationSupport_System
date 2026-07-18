package com.whut.enrollment.vo;

import java.math.BigDecimal;
import java.util.List;

public class CourseStudyScoreResponse {

    private BigDecimal finalScore;
    private String gradeLetter;
    private Integer passed;
    private Integer completedTasks;
    private Integer totalTasks;
    private Integer completedAssignments;
    private Integer totalAssignments;
    private Integer completedExams;
    private Integer totalExams;
    private Integer completionPercent;
    private List<ComponentScoreItem> componentScores;

    public static class ComponentScoreItem {
        private String name;
        private BigDecimal weight;
        private BigDecimal score;
        private Integer maxScore;

        public ComponentScoreItem() {}

        public ComponentScoreItem(String name, BigDecimal weight, BigDecimal score, Integer maxScore) {
            this.name = name;
            this.weight = weight;
            this.score = score;
            this.maxScore = maxScore;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public Integer getMaxScore() { return maxScore; }
        public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public String getGradeLetter() { return gradeLetter; }
    public void setGradeLetter(String gradeLetter) { this.gradeLetter = gradeLetter; }
    public Integer getPassed() { return passed; }
    public void setPassed(Integer passed) { this.passed = passed; }
    public Integer getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Integer completedTasks) { this.completedTasks = completedTasks; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }
    public Integer getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(Integer completedAssignments) { this.completedAssignments = completedAssignments; }
    public Integer getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
    public Integer getCompletedExams() { return completedExams; }
    public void setCompletedExams(Integer completedExams) { this.completedExams = completedExams; }
    public Integer getTotalExams() { return totalExams; }
    public void setTotalExams(Integer totalExams) { this.totalExams = totalExams; }
    public Integer getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(Integer completionPercent) { this.completionPercent = completionPercent; }
    public List<ComponentScoreItem> getComponentScores() { return componentScores; }
    public void setComponentScores(List<ComponentScoreItem> componentScores) { this.componentScores = componentScores; }
}
