package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper。
 *
 * <p>继承 {@link BaseMapper} 后即自动拥有 insert / deleteById / updateById /
 * selectById / selectList / selectPage 等方法，<b>不需要写任何实现类</b>——
 * MyBatis 会在运行时用动态代理生成实现。</p>
 *
 * @author students
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
