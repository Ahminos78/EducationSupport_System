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
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name,
                   coalesce(sp.student_no, cast(s.student_id as char)) as student_no,
                   coalesce(nullif(sp.real_name, ''), nullif(u.nickname, ''), u.username) as student_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            left join tb_user u on u.id = s.student_id
            left join tb_student_profile sp on sp.user_id = s.student_id
            where s.assignment_id = #{assignmentId}
            order by s.id desc
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

    class SubmissionResponseRow extends Submission {
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private String studentNo;
        private String studentName;

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
    }
}
