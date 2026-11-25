package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.ProductLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品点赞 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductLikeMapper extends BaseMapper<ProductLike> {

    /**
     * 物理删除点赞记录。
     *
     * <p>为什么不用继承来的 deleteById：MyBatis-Plus 的 @TableLogic 会把它改写成
     * UPDATE ... SET deleted = 1，记录留在表里继续占用唯一索引 (user_id, product_id)，
     * 导致「取消后再点赞」时 INSERT 触发 DuplicateKeyException。
     * 点赞记录没有保留价值，物理删除最干净。</p>
     *
     * @param userId    用户 ID
     * @param productId 商品 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM product_like WHERE user_id = #{userId} AND product_id = #{productId}")
    int physicalDelete(@Param("userId") Long userId, @Param("productId") Long productId);
}
