package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品图片 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {
}
