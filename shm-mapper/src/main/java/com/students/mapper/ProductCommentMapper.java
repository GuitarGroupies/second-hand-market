package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.ProductComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品评论 Mapper。
 *
 * @author students
 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    /**
     * 原子增加评论的点赞数或踩数。
     *
     * <p>为什么不用「查出来 +1 再更新」：并发下会丢失更新
     * （两个请求都读到 5，都写成 6，实际应为 7）。
     * 直接用 SET col = col + 1 交给数据库做原子自增。</p>
     *
     * @param commentId 评论 ID
     * @param column    列名，仅允许 like_count / dislike_count（由常量保证）
     * @param delta     增量，+1 或 -1
     * @return 影响行数
     */
    @Update("UPDATE product_comment SET ${column} = ${column} + #{delta} "
            + "WHERE id = #{commentId} AND ${column} + #{delta} >= 0")
    int incrCount(@Param("commentId") Long commentId,
                  @Param("column") String column,
                  @Param("delta") int delta);
}