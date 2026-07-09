package com.whut.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.interaction.entity.CourseSnapshot;
import com.whut.interaction.entity.Discussion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DiscussionMapper extends BaseMapper<Discussion> {

    @Select("""
            select d.*, c.name as course_name
            from tb_discussion d
            left join tb_course c on c.id = d.course_id
            where d.course_id = #{courseId}
              and d.parent_id is null
              and d.deleted = 0
              and (#{includeHidden} = true or d.status = 1)
            order by d.id desc
            limit #{offset}, #{size}
            """)
    List<DiscussionResponseRow> findTopics(@Param("courseId") Long courseId,
                                           @Param("offset") int offset,
                                           @Param("size") int size,
                                           @Param("includeHidden") boolean includeHidden);

    @Select("""
            select d.*, c.name as course_name
            from tb_discussion d
            left join tb_course c on c.id = d.course_id
            where d.parent_id = #{topicId}
              and d.deleted = 0
              and (#{includeHidden} = true or d.status = 1)
            order by d.id asc
            limit #{offset}, #{size}
            """)
    List<DiscussionResponseRow> findReplies(@Param("topicId") Long topicId,
                                            @Param("offset") int offset,
                                            @Param("size") int size,
                                            @Param("includeHidden") boolean includeHidden);

    @Select("""
            select d.*, c.name as course_name
            from tb_discussion d
            left join tb_course c on c.id = d.course_id
            where d.id = #{id} and d.deleted = 0
            """)
    DiscussionResponseRow findResponseById(@Param("id") Long id);

    @Select("select id, teacher_id, name, status, deleted from tb_course where id = #{id}")
    CourseSnapshot findCourseById(@Param("id") Long id);

    @Update("""
            update tb_discussion
            set title = #{title},
                content = #{content}
            where id = #{id} and deleted = 0
            """)
    int update(Discussion discussion);

    @Update("update tb_discussion set status = #{status} where id = #{id} and deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    class DiscussionResponseRow extends Discussion {
        private String courseName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }
}
