package com.whut.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.enrollment.entity.GradeComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GradeComponentMapper extends BaseMapper<GradeComponent> {
    @Select("select * from tb_grade_component where course_id = #{courseId} order by sort_order")
    List<GradeComponent> findByCourseId(@Param("courseId") Long courseId);
}
