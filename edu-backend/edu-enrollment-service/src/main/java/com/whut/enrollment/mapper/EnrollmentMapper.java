package com.whut.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.enrollment.entity.CourseSnapshot;
import com.whut.enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    @Select("""
            select e.*, c.name as course_name, u.nickname as student_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            left join tb_user u on u.id = e.student_id
            where e.student_id = #{studentId}
            order by e.id desc
            """)
    List<EnrollmentResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select e.*, c.name as course_name, u.nickname as student_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            left join tb_user u on u.id = e.student_id
            where e.course_id = #{courseId}
              and (#{status} is null or e.status = #{status})
            order by e.id desc
            """)
    List<EnrollmentResponseRow> findByCourseId(@Param("courseId") Long courseId, @Param("status") Integer status);

    @Select("select id, teacher_id, name, max_students, enrolled_count, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

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

    @Select("select count(*) from tb_assignment where course_id = #{courseId} and deleted = 0")
    int countAssignments(@Param("courseId") Long courseId);

    @Select("select count(*) from tb_exam where course_id = #{courseId} and deleted = 0")
    int countExams(@Param("courseId") Long courseId);


    class EnrollmentResponseRow extends Enrollment {
        private String courseName;
        private String studentName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
    }
}
