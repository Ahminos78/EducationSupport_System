package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    @Select("""
            select e.*, c.name as course_name
            from tb_exam e
            left join tb_course c on c.id = e.course_id
            where e.course_id = #{courseId}
              and e.deleted = 0
              and (#{includeDraft} = true or e.status in (1, 2))
            order by e.start_time desc, e.id desc
            """)
    List<ExamResponseRow> findByCourseId(@Param("courseId") Long courseId,
                                         @Param("includeDraft") boolean includeDraft);

    @Select("""
            select e.*, c.name as course_name
            from tb_exam e
            left join tb_course c on c.id = e.course_id
            where e.id = #{id} and e.deleted = 0
            """)
    ExamResponseRow findResponseById(@Param("id") Long id);

    @Select("select id, teacher_id, name, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

    @Select("""
            select count(1) > 0
            from tb_enrollment
            where course_id = #{courseId}
              and student_id = #{studentId}
              and status = 1
            """)
    boolean isApprovedStudent(@Param("courseId") Long courseId,
                              @Param("studentId") Long studentId);

    class ExamResponseRow extends Exam {
        private String courseName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }
}
