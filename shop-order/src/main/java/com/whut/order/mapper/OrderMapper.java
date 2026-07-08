package com.whut.order.mapper;

import com.whut.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    @Select("SELECT id, user_id, product_name, price FROM t_order WHERE id = #{id}")
    Order selectById(Long id);
}
