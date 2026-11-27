package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper。
 *
 * @author students
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
