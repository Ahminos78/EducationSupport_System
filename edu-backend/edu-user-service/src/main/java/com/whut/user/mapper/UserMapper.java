package com.whut.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from tb_user where deleted = 0 order by id desc limit #{offset}, #{size}")
    List<User> findPage(@Param("offset") int offset, @Param("size") int size);

    @Update("""
            update tb_user
            set password_hash = #{passwordHash},
                nickname = #{nickname},
                role = #{role}
            where id = #{id} and deleted = 0
            """)
    int update(User user);
}
