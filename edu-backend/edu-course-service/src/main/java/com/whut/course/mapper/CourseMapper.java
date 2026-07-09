package com.whut.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.course.entity.Course;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
            select * from tb_course
            where deleted = 0
              and (#{status} is null or status = #{status})
              and (#{teacherId} is null or teacher_id = #{teacherId})
            order by id desc
            limit #{offset}, #{size}
            """)
    List<Course> findPage(@Param("offset") int offset,
                          @Param("size") int size,
                          @Param("status") Integer status,
                          @Param("teacherId") Long teacherId);

    @Insert("""
            insert into tb_course (teacher_id, name, description, cover_url, max_students, status)
            values (#{teacherId}, #{name}, #{description}, #{coverUrl}, #{maxStudents}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

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
}
