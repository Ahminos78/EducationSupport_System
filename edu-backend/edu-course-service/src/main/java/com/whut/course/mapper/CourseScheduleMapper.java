package com.whut.course.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseScheduleMapper {

    @Select("""
            SELECT id, class_id, day_of_week, start_period, end_period,
                   start_week, end_week, week_type, location
            FROM tb_course_schedule
            WHERE class_id = #{classId}
            ORDER BY day_of_week, start_period
            """)
    List<ScheduleItem> findByClassId(@Param("classId") Long classId);

    class ScheduleItem {
        private Long id;
        private Long classId;
        private Integer dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
        private Integer startWeek;
        private Integer endWeek;
        private Integer weekType;
        private String location;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public Integer getStartPeriod() { return startPeriod; }
        public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }
        public Integer getEndPeriod() { return endPeriod; }
        public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }
        public Integer getStartWeek() { return startWeek; }
        public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
        public Integer getEndWeek() { return endWeek; }
        public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
        public Integer getWeekType() { return weekType; }
        public void setWeekType(Integer weekType) { this.weekType = weekType; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
