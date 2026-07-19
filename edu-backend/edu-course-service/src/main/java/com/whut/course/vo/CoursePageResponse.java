package com.whut.course.vo;

import java.util.List;

public class CoursePageResponse {

    private List<CourseResponse> records;
    private long total;

    public CoursePageResponse(List<CourseResponse> records, long total) {
        this.records = records;
        this.total = total;
    }

    public List<CourseResponse> getRecords() { return records; }
    public void setRecords(List<CourseResponse> records) { this.records = records; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
