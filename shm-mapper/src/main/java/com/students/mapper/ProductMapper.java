package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 给浏览量累加指定值。
     *
     * <p>为什么用 {@code view_count = view_count + #{delta}} 而不是先查后写：
     * 后者在并发下会丢失更新。交给数据库做原子自增才安全。</p>
     *
     * @param productId 商品 ID
     * @param delta     增量
     * @return 影响行数
     */
    @Update("UPDATE product SET view_count = view_count + #{delta} WHERE id = #{productId}")
    int incrViewCount(@Param("productId") Long productId, @Param("delta") int delta);
}
