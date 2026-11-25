package com.students.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.students.pojo.entity.CommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论点赞踩 Mapper。
 *
 * @author students
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 物理删除投票记录（原因同 ProductLikeMapper#physicalDelete：
     * 逻辑删除会继续占用唯一索引 (user_id, comment_id)，导致再次投票插入失败）。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int physicalDelete(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * 切换投票类型（赞 ↔ 踩），直接改 type 字段。
     *
     * @param id      记录主键
     * @param newType 新的类型：1 赞，2 踩
     * @return 影响行数
     */
    @Update("UPDATE comment_like SET type = #{newType}, update_time = NOW() WHERE id = #{id}")
    int switchType(@Param("id") Long id, @Param("newType") Integer newType);
}
