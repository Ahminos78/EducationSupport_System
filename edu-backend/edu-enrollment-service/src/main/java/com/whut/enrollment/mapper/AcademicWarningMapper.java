package com.whut.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.enrollment.entity.AcademicWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AcademicWarningMapper extends BaseMapper<AcademicWarning> {

    @Select("""
            select w.*, c.name as course_name
            from tb_academic_warning w
            left join tb_course c on c.id = w.course_id
            where w.student_id = #{studentId}
            order by w.created_at desc
            """)
    List<WarningRow> findByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select w.*, c.name as course_name
            from tb_academic_warning w
            left join tb_course c on c.id = w.course_id
            where w.student_id = #{studentId} and w.status < 2
            order by w.created_at desc
            """)
    List<WarningRow> findActiveByStudentId(@Param("studentId") Long studentId);

    @Select("""
            select count(*) from tb_academic_warning
            where student_id = #{studentId} and course_id = #{courseId}
              and warning_type = #{warningType} and status < 2
            """)
    int countActiveByStudentAndCourse(@Param("studentId") Long studentId,
                                      @Param("courseId") Long courseId,
                                      @Param("warningType") Integer warningType);

    class WarningRow extends AcademicWarning {
        private String courseName;

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
    }
}
