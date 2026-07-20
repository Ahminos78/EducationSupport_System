package com.whut.course.dto;

public class ScheduleSlotRequest {
    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private String location;

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getStartPeriod() { return startPeriod; }
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }
    public Integer getEndPeriod() { return endPeriod; }
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
