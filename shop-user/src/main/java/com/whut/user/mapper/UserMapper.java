package com.whut.user.mapper;

import com.whut.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, email FROM t_user WHERE id = #{id}")
    User selectById(Long id);
}
