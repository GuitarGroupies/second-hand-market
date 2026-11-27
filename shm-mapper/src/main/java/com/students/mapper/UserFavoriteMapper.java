package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.UserFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品收藏 Mapper。
 *
 * @author students
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    /**
     * 物理删除收藏记录。
     *
     * <p>与点赞同理：逻辑删除会继续占用唯一索引 (user_id, product_id)，
     * 导致「取消后重新收藏」插入失败，所以用物理删除。</p>
     *
     * @param userId    用户 ID
     * @param productId 商品 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int physicalDelete(@Param("userId") Long userId, @Param("productId") Long productId);
}