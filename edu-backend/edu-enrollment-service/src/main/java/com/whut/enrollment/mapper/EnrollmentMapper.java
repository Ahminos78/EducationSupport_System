package com.whut.enrollment.mapper;

import com.whut.enrollment.entity.CourseSnapshot;
import com.whut.enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EnrollmentMapper {

    @Select("""
            select e.*, c.name as course_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            where e.student_id = #{studentId}
            order by e.id desc
            """)
    List<EnrollmentResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select e.*, c.name as course_name
            from tb_enrollment e
            left join tb_course c on c.id = e.course_id
            where e.course_id = #{courseId}
              and (#{status} is null or e.status = #{status})
            order by e.id desc
            """)
    List<EnrollmentResponseRow> findByCourseId(@Param("courseId") Long courseId, @Param("status") Integer status);

    @Select("select * from tb_enrollment where id = #{id}")
    Enrollment findById(@Param("id") Long id);

    @Select("select * from tb_enrollment where course_id = #{courseId} and student_id = #{studentId}")
    Enrollment findByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("select id, teacher_id, name, max_students, enrolled_count, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

    @Insert("""
            insert into tb_enrollment (course_id, student_id, status, apply_reason)
            values (#{courseId}, #{studentId}, #{status}, #{applyReason})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Enrollment enrollment);

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

    class EnrollmentResponseRow extends Enrollment {
        private String courseName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }
}
