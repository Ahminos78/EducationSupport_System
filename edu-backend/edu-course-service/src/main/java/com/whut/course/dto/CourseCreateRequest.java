package com.whut.course.dto;

import java.math.BigDecimal;

public class CourseCreateRequest {

    private String name;
    private String description;
    private String coverUrl;
    private Integer maxStudents;
    private BigDecimal credit;
    private String dept;
    private String category;
    private String tags;
    private Integer classCount;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }

    public java.math.BigDecimal getCredit() { return credit; }
    public void setCredit(java.math.BigDecimal credit) { this.credit = credit; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getClassCount() { return classCount; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
