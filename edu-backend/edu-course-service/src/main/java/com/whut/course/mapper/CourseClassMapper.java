package com.whut.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.course.entity.CourseClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {

    @Select("""
            SELECT cc.id, cc.course_id, cc.teacher_id, cc.name, cc.max_students,
                   cc.enrolled_count,
                   coalesce(nullif(u.nickname, ''), u.username) AS teacher_name,
                   u.username AS teacher_username
            FROM tb_course_class cc
            LEFT JOIN tb_user u ON u.id = cc.teacher_id
            WHERE cc.course_id = #{courseId}
              AND cc.deleted = 0
            ORDER BY cc.id
            """)
    List<CourseClassResponse> findByCourseId(@Param("courseId") Long courseId);

    class CourseClassResponse {
        private Long id;
        private Long courseId;
        private Long teacherId;
        private String name;
        private Integer maxStudents;
        private Integer enrolledCount;
        private String teacherName;
        private String teacherUsername;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getMaxStudents() { return maxStudents; }
        public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
        public Integer getEnrolledCount() { return enrolledCount; }
        public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public String getTeacherUsername() { return teacherUsername; }
        public void setTeacherUsername(String teacherUsername) { this.teacherUsername = teacherUsername; }
    }
}
