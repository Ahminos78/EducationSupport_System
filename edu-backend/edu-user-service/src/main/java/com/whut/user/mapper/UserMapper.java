package com.whut.user.mapper;

import com.whut.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from tb_user where deleted = 0 order by id desc limit #{offset}, #{size}")
    List<User> findPage(@Param("offset") int offset, @Param("size") int size);

    @Select("select count(*) from tb_user")
    long countAll();

    @Select("select * from tb_user where id = #{id} and deleted = 0")
    User findById(@Param("id") Long id);

    @Select("select * from tb_user where username = #{username} and deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("select count(*) from tb_user where username = #{username} and deleted = 0")
    int countByUsername(@Param("username") String username);

    @Insert("""
            insert into tb_user (username, password_hash, nickname, role)
            values (#{username}, #{passwordHash}, #{nickname}, #{role})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
            update tb_user
            set password_hash = #{passwordHash},
                nickname = #{nickname},
                role = #{role}
            where id = #{id} and deleted = 0
            """)
    int update(User user);

    @Update("update tb_user set deleted = 1 where id = #{id} and deleted = 0")
    int logicalDelete(@Param("id") Long id);
}
