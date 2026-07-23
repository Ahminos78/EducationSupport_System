package com.whut.course.service;

import com.whut.common.exception.BusinessException;
import com.whut.course.dto.ScheduleSlotRequest;
import com.whut.course.entity.Course;
import com.whut.course.entity.CourseClass;
import com.whut.course.entity.CourseSchedule;
import com.whut.course.mapper.CourseClassMapper;
import com.whut.course.mapper.CourseMapper;
import com.whut.course.mapper.CourseScheduleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseClassService {

    private final CourseClassMapper courseClassMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;

    public CourseClassService(CourseClassMapper courseClassMapper,
                              CourseScheduleMapper courseScheduleMapper,
                              CourseMapper courseMapper) {
        this.courseClassMapper = courseClassMapper;
        this.courseScheduleMapper = courseScheduleMapper;
        this.courseMapper = courseMapper;
    }

    public CourseClass createClassSection(Long courseId, Long teacherId, Integer maxStudents,
                                          List<ScheduleSlotRequest> scheduleSlots) {
        long count = courseClassMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseClass>()
                        .eq(CourseClass::getCourseId, courseId)
                        .eq(CourseClass::getDeleted, 0)
        );
        Course course = courseMapper.selectById(courseId);
        String className = (course != null ? course.getName() : "课程") + " 第" + (count + 1) + "班";
        CourseClass classSection = new CourseClass();
        classSection.setCourseId(courseId);
        classSection.setTeacherId(teacherId);
        classSection.setName(className);
        classSection.setMaxStudents(maxStudents);
        classSection.setEnrolledCount(0);
        courseClassMapper.insert(classSection);
        if (scheduleSlots != null && !scheduleSlots.isEmpty()) {
            createScheduleSlots(classSection.getId(), teacherId, scheduleSlots);
        }
        courseMapper.updateClassCount(courseId);
        return classSection;
    }

    public void createScheduleSlots(Long classId, Long teacherId, List<ScheduleSlotRequest> slots) {
        for (ScheduleSlotRequest slot : slots) {
            if (slot.getDayOfWeek() == null || slot.getStartPeriod() == null
                    || slot.getEndPeriod() == null) {
                throw BusinessException.badRequest("排课信息不完整");
            }
            if (slot.getStartPeriod() > slot.getEndPeriod()) {
                throw BusinessException.badRequest("开始节次不能大于结束节次");
            }
            if (slot.getLocation() == null || slot.getLocation().trim().isEmpty()) {
                throw BusinessException.badRequest("上课地点不能为空");
            }
            int conflicts = courseScheduleMapper.countConflicts(
                    slot.getDayOfWeek(), slot.getStartPeriod(), slot.getEndPeriod(),
                    slot.getLocation().trim(), teacherId);
            if (conflicts > 0) {
                throw BusinessException.badRequest("排课冲突：该时间段教室已被占用或教师已有其他课程");
            }
            CourseSchedule cs = new CourseSchedule();
            cs.setClassId(classId);
            cs.setDayOfWeek(slot.getDayOfWeek());
            cs.setStartPeriod(slot.getStartPeriod());
            cs.setEndPeriod(slot.getEndPeriod());
            cs.setStartWeek(1);
            cs.setEndWeek(16);
            cs.setWeekType(0);
            cs.setLocation(slot.getLocation().trim());
            courseScheduleMapper.insert(cs);
        }
    }

    public void deleteClassSection(Long classId, Long teacherId) {
        CourseClass classSection = courseClassMapper.selectById(classId);
        if (classSection == null || (classSection.getDeleted() != null && classSection.getDeleted() == 1)) {
            throw BusinessException.notFound("教学班不存在");
        }
        if (!classSection.getTeacherId().equals(teacherId)) {
            throw BusinessException.forbidden("只能删除自己的教学班");
        }
        if (classSection.getEnrolledCount() != null && classSection.getEnrolledCount() > 0) {
            throw BusinessException.badRequest("该教学班已有学生，无法删除");
        }
        classSection.setDeleted(1);
        courseClassMapper.updateById(classSection);
        courseMapper.updateClassCount(classSection.getCourseId());
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
