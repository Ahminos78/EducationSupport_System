package com.whut.course.service;

import com.whut.course.mapper.CourseClassMapper;
import com.whut.course.mapper.CourseScheduleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseClassService {

    private final CourseClassMapper courseClassMapper;
    private final CourseScheduleMapper courseScheduleMapper;

    public CourseClassService(CourseClassMapper courseClassMapper, CourseScheduleMapper courseScheduleMapper) {
        this.courseClassMapper = courseClassMapper;
        this.courseScheduleMapper = courseScheduleMapper;
    }

    public List<CourseClassWithSchedule> getClassesByCourse(Long courseId) {
        List<CourseClassMapper.CourseClassResponse> classes = courseClassMapper.findByCourseId(courseId);
        return classes.stream().map(cls -> {
            CourseClassWithSchedule result = new CourseClassWithSchedule();
            result.setId(cls.getId());
            result.setCourseId(cls.getCourseId());
            result.setTeacherId(cls.getTeacherId());
            result.setTeacherName(cls.getTeacherName());
            result.setName(cls.getName());
            result.setMaxStudents(cls.getMaxStudents());
            result.setEnrolledCount(cls.getEnrolledCount());
            List<CourseScheduleMapper.ScheduleItem> rawSchedule = courseScheduleMapper.findByClassId(cls.getId());
            List<ScheduleItem> scheduleItems = rawSchedule.stream().map(s -> {
                ScheduleItem item = new ScheduleItem();
                item.setId(s.getId());
                item.setClassId(s.getClassId());
                item.setDayOfWeek(s.getDayOfWeek());
                item.setStartPeriod(s.getStartPeriod());
                item.setEndPeriod(s.getEndPeriod());
                item.setStartWeek(s.getStartWeek());
                item.setEndWeek(s.getEndWeek());
                item.setWeekType(s.getWeekType());
                item.setLocation(s.getLocation());
                return item;
            }).toList();
            result.setSchedule(scheduleItems);
            return result;
        }).toList();
    }

    public static class CourseClassWithSchedule {
        private Long id;
        private Long courseId;
        private Long teacherId;
        private String teacherName;
        private String name;
        private Integer maxStudents;
        private Integer enrolledCount;
        private List<ScheduleItem> schedule;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getMaxStudents() { return maxStudents; }
        public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
        public Integer getEnrolledCount() { return enrolledCount; }
        public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
        public List<ScheduleItem> getSchedule() { return schedule; }
        public void setSchedule(List<ScheduleItem> schedule) { this.schedule = schedule; }
    }

    public static class ScheduleItem {
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
