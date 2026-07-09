package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.CourseSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {

    @Select("""
            select a.*, c.name as course_name
            from tb_assignment a
            left join tb_course c on c.id = a.course_id
            where a.course_id = #{courseId}
              and a.deleted = 0
              and (#{includeDraft} = true or a.status in (1, 2))
            order by a.id desc
            """)
    List<AssignmentResponseRow> findByCourseId(@Param("courseId") Long courseId,
                                               @Param("includeDraft") boolean includeDraft);

    @Select("""
            select a.*, c.name as course_name
            from tb_assignment a
            left join tb_course c on c.id = a.course_id
            where a.id = #{id} and a.deleted = 0
            """)
    AssignmentResponseRow findResponseById(@Param("id") Long id);

    @Select("select id, teacher_id, name, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

    @Insert("""
            insert into tb_assignment (course_id, teacher_id, title, description, full_score, deadline, status)
            values (#{courseId}, #{teacherId}, #{title}, #{description}, #{fullScore}, #{deadline}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);

    @Update("""
            update tb_assignment
            set title = #{title},
                description = #{description},
                full_score = #{fullScore},
                deadline = #{deadline}
            where id = #{id} and deleted = 0
            """)
    int update(Assignment assignment);

    @Update("update tb_assignment set status = #{status} where id = #{id} and deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    class AssignmentResponseRow extends Assignment {
        private String courseName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }
}
