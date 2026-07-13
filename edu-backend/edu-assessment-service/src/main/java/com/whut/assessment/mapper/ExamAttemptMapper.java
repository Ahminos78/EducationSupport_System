package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.ExamAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExamAttemptMapper extends BaseMapper<ExamAttempt> {

    @Select("""
            select attempt.*, exam.title as exam_title, exam.course_id,
                   course.name as course_name,
                   user_record.nickname as student_name
            from tb_exam_attempt attempt
            join tb_exam exam on exam.id = attempt.exam_id
            join tb_course course on course.id = exam.course_id
            join tb_user user_record on user_record.id = attempt.student_id
            where attempt.id = #{id}
            """)
    ExamAttemptResponseRow findResponseById(@Param("id") Long id);

    @Select("""
            select attempt.*, exam.title as exam_title, exam.course_id,
                   course.name as course_name,
                   user_record.nickname as student_name
            from tb_exam_attempt attempt
            join tb_exam exam on exam.id = attempt.exam_id
            join tb_course course on course.id = exam.course_id
            join tb_user user_record on user_record.id = attempt.student_id
            where attempt.student_id = #{studentId}
            order by attempt.id desc
            """)
    List<ExamAttemptResponseRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select attempt.*, exam.title as exam_title, exam.course_id,
                   course.name as course_name,
                   user_record.nickname as student_name
            from tb_exam_attempt attempt
            join tb_exam exam on exam.id = attempt.exam_id
            join tb_course course on course.id = exam.course_id
            join tb_user user_record on user_record.id = attempt.student_id
            where attempt.exam_id = #{examId}
            order by attempt.id desc
            """)
    List<ExamAttemptResponseRow> findByExamId(@Param("examId") Long examId);

    @Select("""
            select * from tb_exam_attempt
            where exam_id = #{examId} and student_id = #{studentId}
            """)
    ExamAttempt findByExamAndStudent(@Param("examId") Long examId,
                                     @Param("studentId") Long studentId);

    @Update("""
            update tb_exam_attempt
            set answer_content = #{answerContent},
                status = 1,
                submitted_at = current_timestamp
            where id = #{id} and status = 0
            """)
    int submit(@Param("id") Long id, @Param("answerContent") String answerContent);

    @Update("""
            update tb_exam_attempt
            set score = #{score},
                teacher_comment = #{teacherComment},
                status = 2,
                graded_at = current_timestamp
            where id = #{id} and status in (1, 2)
            """)
    int grade(@Param("id") Long id,
              @Param("score") Integer score,
              @Param("teacherComment") String teacherComment);

    class ExamAttemptResponseRow extends ExamAttempt {
        private String examTitle;
        private Long courseId;
        private String courseName;
        private String studentName;

        public String getExamTitle() {
            return examTitle;
        }

        public void setExamTitle(String examTitle) {
            this.examTitle = examTitle;
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

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
    }
}
