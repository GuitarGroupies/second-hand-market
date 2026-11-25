package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.ProductWant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品想要 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductWantMapper extends BaseMapper<ProductWant> {

    /**
     * 物理删除想要记录（原因同 {@link ProductLikeMapper#physicalDelete}）。
     *
     * @param userId    用户 ID
     * @param productId 商品 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM product_want WHERE user_id = #{userId} AND product_id = #{productId}")
    int physicalDelete(@Param("userId") Long userId, @Param("productId") Long productId);
}
