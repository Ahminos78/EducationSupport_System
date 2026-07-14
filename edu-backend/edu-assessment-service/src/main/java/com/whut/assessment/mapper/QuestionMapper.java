package com.whut.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.assessment.entity.Question;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("select * from tb_question where exam_id = #{examId} order by sort_order, id")
    List<Question> findByExamId(@Param("examId") Long examId);

    @Delete("delete from tb_question where exam_id = #{examId}")
    int deleteByExamId(@Param("examId") Long examId);
}
