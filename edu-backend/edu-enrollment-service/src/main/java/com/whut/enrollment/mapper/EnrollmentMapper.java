package com.whut.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.enrollment.entity.CourseSnapshot;
import com.whut.enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    @Select("""
            select e.*, c.name as course_name, cc.name as class_name, u.nickname as student_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            left join tb_course_class cc on cc.id = e.class_id
            left join tb_user u on u.id = e.student_id
            where e.student_id = #{studentId}
            order by e.id desc
            """)
    List<EnrollmentResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select e.*, c.name as course_name, cc.name as class_name, u.nickname as student_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            left join tb_course_class cc on cc.id = e.class_id
            left join tb_user u on u.id = e.student_id
            where e.course_id = #{courseId}
              and (#{classId} is null or e.class_id = #{classId})
              and (#{status} is null or e.status = #{status})
            order by e.class_id, e.id desc
            """)
    List<EnrollmentResponseRow> findByCourseId(@Param("courseId") Long courseId,
                                               @Param("classId") Long classId,
                                               @Param("status") Integer status);

    @Select("select id, teacher_id, name, max_students, enrolled_count, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

    @Select("""
            select id, course_id, teacher_id, name, max_students, enrolled_count
            from tb_course_class
            where id = #{id}
            """)
    ClassSnapshot findClassById(@Param("id") Long id);

    @Update("""
            update tb_enrollment
            set status = #{status},
                review_comment = #{reviewComment},
                reviewed_at = current_timestamp
            where id = #{id}
            """)
    int updateReviewStatus(@Param("id") Long id,
                           @Param("status") Integer status,
                           @Param("reviewComment") String reviewComment);

    @Update("""
            update tb_enrollment
            set status = 2,
                reviewed_at = current_timestamp
            where id = #{id}
            """)
    int drop(@Param("id") Long id);

    @Delete("delete from tb_enrollment where id = #{id}")
    int physicallyDelete(@Param("id") Long id);

    @Update("""
            update tb_course
            set enrolled_count = enrolled_count + 1
            where id = #{courseId}
              and deleted = 0
              and status = 1
              and enrolled_count < max_students
            """)
    int increaseCourseEnrollment(@Param("courseId") Long courseId);

    @Update("""
            update tb_course
            set enrolled_count = greatest(enrolled_count - 1, 0)
            where id = #{courseId} and deleted = 0
            """)
    int decreaseCourseEnrollment(@Param("courseId") Long courseId);

    // ── 学业成绩相关 ────────────────────────────────────────
    @Select("""
            select s.score from tb_submission s
            join tb_assignment a on a.id = s.assignment_id
            where a.course_id = #{courseId} and s.student_id = #{studentId}
              and s.score is not null and s.grading_status = 1
            """)
    List<Integer> findSubmissionScores(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("""
            select ea.score from tb_exam_attempt ea
            join tb_exam e on e.id = ea.exam_id
            where e.course_id = #{courseId} and ea.student_id = #{studentId}
              and ea.score is not null
            """)
    List<Integer> findExamScores(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("""
            select ea.score from tb_exam_attempt ea
            join tb_exam e on e.id = ea.exam_id
            where e.course_id = #{courseId} and ea.student_id = #{studentId}
              and ea.score is not null and e.type = #{type}
            """)
    List<Integer> findExamScoresByType(@Param("courseId") Long courseId,
                                       @Param("studentId") Long studentId,
                                       @Param("type") String type);

    @Select("select count(*) from tb_assignment where course_id = #{courseId} and deleted = 0")
    int countAssignments(@Param("courseId") Long courseId);

    @Select("select count(*) from tb_exam where course_id = #{courseId} and deleted = 0")
    int countExams(@Param("courseId") Long courseId);

    @Select("select count(*) from tb_exam where course_id = #{courseId} and deleted = 0 and type = #{type}")
    int countExamsByType(@Param("courseId") Long courseId, @Param("type") String type);

    // ── 教学班选课 + 冲突检测 ────────────────────────────────
    @Update("""
            update tb_course_class
            set enrolled_count = enrolled_count + 1
            where id = #{classId}
            """)
    int increaseClassEnrollment(@Param("classId") Long classId);

    @Update("""
            update tb_course_class
            set enrolled_count = greatest(enrolled_count - 1, 0)
            where id = #{classId}
            """)
    int decreaseClassEnrollment(@Param("classId") Long classId);

    @Select("select count(*) from tb_course_class where course_id = #{courseId} and teacher_id = #{teacherId} and deleted = 0")
    int countClassesByTeacher(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);

    @Select("select id from tb_course_class where course_id = #{courseId} and teacher_id = #{teacherId} and deleted = 0")
    List<Long> findClassIdsByTeacher(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);

    @Select("<script>"
            + "select e.*, c.name as course_name, cc.name as class_name, u.nickname as student_name "
            + "from tb_enrollment e "
            + "left join tb_course c on c.id = e.course_id "
            + "left join tb_course_class cc on cc.id = e.class_id "
            + "left join tb_user u on u.id = e.student_id "
            + "where e.course_id = #{courseId} "
            + "and e.class_id in "
            + "<foreach collection='classIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach> "
            + "and (#{status} is null or e.status = #{status}) "
            + "order by e.class_id, e.id desc"
            + "</script>")
    List<EnrollmentResponseRow> findByCourseIdAndClassIds(@Param("courseId") Long courseId,
                                                          @Param("classIds") List<Long> classIds,
                                                          @Param("status") Integer status);

    @Select("""
            select s.day_of_week, s.start_period, s.end_period, s.start_week, s.end_week, s.week_type
            from tb_course_schedule s
            where s.class_id = #{classId}
            """)
    List<ScheduleSlot> findScheduleSlotsByClassId(@Param("classId") Long classId);

    @Select("""
            select e.class_id
            from tb_enrollment e
            where e.student_id = #{studentId}
              and e.status = 1
            """)
    List<Long> findEnrolledClassIds(@Param("studentId") Long studentId);

    class ClassSnapshot {
        private Long id;
        private Long courseId;
        private Long teacherId;
        private String name;
        private Integer maxStudents;
        private Integer enrolledCount;

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
    }

    class ScheduleSlot {
        private Integer dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
        private Integer startWeek;
        private Integer endWeek;
        private Integer weekType;

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
    }

    class EnrollmentResponseRow extends Enrollment {
        private String courseName;
        private String className;
        private String studentName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
    }
}
