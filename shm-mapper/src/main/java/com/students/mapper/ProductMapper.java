package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
