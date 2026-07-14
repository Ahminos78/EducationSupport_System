package com.whut.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
            select course.*, coalesce(nullif(user_record.nickname, ''), user_record.username) as teacher_name
            from tb_course course
            left join tb_user user_record on user_record.id = course.teacher_id
            where course.deleted = 0
              and (#{status} is null or course.status = #{status})
              and (#{teacherId} is null or course.teacher_id = #{teacherId})
            order by course.id desc
            limit #{offset}, #{size}
            """)
    List<CourseResponseRow> findPage(@Param("offset") int offset,
                                     @Param("size") int size,
                                     @Param("status") Integer status,
                                     @Param("teacherId") Long teacherId);

    @Select("""
            select course.*, coalesce(nullif(user_record.nickname, ''), user_record.username) as teacher_name
            from tb_course course
            left join tb_user user_record on user_record.id = course.teacher_id
            where course.id = #{id} and course.deleted = 0
            """)
    CourseResponseRow findResponseById(@Param("id") Long id);

    @Update("""
            update tb_course
            set name = #{name},
                description = #{description},
                cover_url = #{coverUrl},
                max_students = #{maxStudents}
            where id = #{id} and deleted = 0
            """)
    int update(Course course);

    @Update("update tb_course set status = #{status} where id = #{id} and deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    class CourseResponseRow extends Course {
        private String teacherName;

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }
    }
}
