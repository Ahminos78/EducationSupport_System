package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.Submission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("""
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            where s.student_id = #{studentId}
            order by s.id desc
            """)
    List<SubmissionResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            where s.assignment_id = #{assignmentId}
            order by s.id desc
            """)
    List<SubmissionResponseRow> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("""
            select s.*, a.title as assignment_title, a.course_id, c.name as course_name
            from tb_submission s
            left join tb_assignment a on a.id = s.assignment_id
            left join tb_course c on c.id = a.course_id
            where s.id = #{id}
            """)
    SubmissionResponseRow findResponseById(@Param("id") Long id);

    @Insert("""
            insert into tb_submission (assignment_id, student_id, content, attachment_url)
            values (#{assignmentId}, #{studentId}, #{content}, #{attachmentUrl})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);

    @Update("""
            update tb_submission
            set content = #{content},
                attachment_url = #{attachmentUrl},
                submitted_at = current_timestamp
            where id = #{id}
            """)
    int updateContent(Submission submission);

    @Update("""
            update tb_submission
            set score = #{score},
                teacher_comment = #{teacherComment},
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
    }
}
