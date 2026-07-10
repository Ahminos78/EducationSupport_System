package com.whut.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whut.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("""
            update tb_user
            set password_hash = #{passwordHash},
                nickname = #{nickname},
                role = #{role}
            where id = #{id} and deleted = 0
            """)
    int update(User user);
}
