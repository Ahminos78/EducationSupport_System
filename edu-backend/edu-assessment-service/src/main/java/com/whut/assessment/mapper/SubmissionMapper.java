package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("""
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name,
                   coalesce(sp.student_no, cast(s.student_id as char)) as student_no,
                   coalesce(nullif(sp.real_name, ''), nullif(u.nickname, ''), u.username) as student_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            left join tb_user u on u.id = s.student_id
            left join tb_student_profile sp on sp.user_id = s.student_id
            where s.student_id = #{studentId}
            order by s.id desc
            """)
    List<SubmissionResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select s.id, a.id as assignment_id, a.title as assignment_title,
                   a.course_id, c.name as course_name, enrollment.student_id,
                   coalesce(sp.student_no, cast(enrollment.student_id as char)) as student_no,
                   coalesce(nullif(sp.real_name, ''), nullif(u.nickname, ''), u.username) as student_name,
                   sp.major, sp.class_name, enrollment.reviewed_at as enrolled_at,
                   s.content, s.attachment_url, s.status, s.grading_status, s.score,
                   s.teacher_comment, s.ai_comment, s.submitted_at, s.graded_at,
                   s.created_at, s.updated_at
            from tb_enrollment enrollment
            join tb_assignment a on a.id = #{assignmentId} and a.course_id = enrollment.course_id
            join tb_course c on c.id = a.course_id
            join tb_user u on u.id = enrollment.student_id and u.deleted = 0
            left join tb_student_profile sp on sp.user_id = enrollment.student_id
            left join tb_submission s
                   on s.assignment_id = a.id and s.student_id = enrollment.student_id
            where enrollment.status = 1
            order by s.submitted_at desc, enrollment.student_id
            """)
    List<SubmissionResponseRow> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("""
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name,
                   coalesce(sp.student_no, cast(s.student_id as char)) as student_no,
                   coalesce(nullif(sp.real_name, ''), nullif(u.nickname, ''), u.username) as student_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            left join tb_user u on u.id = s.student_id
            left join tb_student_profile sp on sp.user_id = s.student_id
            where s.id = #{id}
            """)
    SubmissionResponseRow findResponseById(@Param("id") Long id);

    @Update("""
            update tb_submission
            set content = #{content},
                attachment_url = #{attachmentUrl},
                status = 1,
                grading_status = 0,
                score = null,
                teacher_comment = null,
                graded_at = null,
                submitted_at = current_timestamp
            where id = #{id}
            """)
    int updateContent(Submission submission);

    @Update("""
            update tb_submission
            set score = #{score},
                teacher_comment = #{teacherComment},
                grading_status = 1,
                graded_at = current_timestamp
            where id = #{id}
            """)
    int grade(@Param("id") Long id,
              @Param("score") Integer score,
              @Param("teacherComment") String teacherComment);

    @Update("""
            update tb_submission
            set ai_comment = #{aiComment}
            where id = #{id}
            """)
    int updateAiComment(@Param("id") Long id,
                        @Param("aiComment") String aiComment);

    class SubmissionResponseRow extends Submission {
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private String studentNo;
        private String studentName;
        private String major;
        private String className;
        private java.time.LocalDateTime enrolledAt;

        public String getAssignmentTitle() {
            return assignmentTitle;
        }

        public void setAssignmentTitle(String assignmentTitle) {
            this.assignmentTitle = assignmentTitle;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public java.time.LocalDateTime getEnrolledAt() {
            return enrolledAt;
        }

        public void setEnrolledAt(java.time.LocalDateTime enrolledAt) {
            this.enrolledAt = enrolledAt;
        }
    }
}
