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
            select count(*)
            from tb_course course
            where course.deleted = 0
              and (#{status} is null or course.status = #{status})
              and (#{teacherId} is null or course.teacher_id = #{teacherId})
            """)
    long countPage(@Param("status") Integer status,
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

    @Update("""
            update tb_course c
            set c.class_count = (select count(*) from tb_course_class cc where cc.course_id = c.id and cc.deleted = 0)
            where c.id = #{courseId}
            """)
    int updateClassCount(@Param("courseId") Long courseId);

    @Select("""
            select distinct course.*,
                   coalesce(nullif(user_record.nickname, ''), user_record.username) as teacher_name
            from tb_course course
            left join tb_user user_record on user_record.id = course.teacher_id
            left join tb_course_class cc on cc.course_id = course.id and cc.deleted = 0
            where course.deleted = 0
              and (course.teacher_id = #{teacherId} or cc.teacher_id = #{teacherId})
            order by course.id
            """)
    List<CourseResponseRow> findMyTaughtCourses(@Param("teacherId") Long teacherId);

    @Select("select * from tb_course where name = #{name} and deleted = 0 limit 1")
    Course findByName(@Param("name") String name);

    @Select("""
            select id, name from tb_course
            where deleted = 0 and name like concat('%', #{q}, '%')
            order by id limit 10
            """)
    List<CourseSuggestion> searchByName(@Param("q") String q);

    class CourseResponseRow extends Course {
        private String teacherName;

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }
    }

    class CourseSuggestion {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
