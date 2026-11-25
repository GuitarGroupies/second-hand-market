package com.students.service;

import com.students.pojo.vo.LikeResultVO;

/**
 * 互动业务接口：商品点赞、想要、评论点赞踩。
 *
 * @author students
 */
public interface InteractionService {

    /**
     * 切换商品点赞状态（已赞则取消）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID
     * @return 操作后的状态与总数
     */
    LikeResultVO toggleProductLike(Long productId, Long userId);

    /**
     * 切换商品想要状态（已想要则取消）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID
     * @return 操作后的状态与总数
     */
    LikeResultVO toggleProductWant(Long productId, Long userId);

    /**
     * 对评论点赞或踩。
     *
     * <p>三种操作：赞（1）、踩（2）、取消（0）。同一个用户的赞和踩互斥，
     * 给已赞的评论点踩，会自动撤销赞。</p>
     *
     * @param commentId 评论 ID
     * @param userId    当前用户 ID
     * @param type      目标类型：1 赞，2 踩，0 取消
     * @return 操作后的状态：liked=true 表示已赞，count 为赞数；dislikeCount 见下方说明
     */
    LikeResultVO toggleCommentLike(Long commentId, Long userId, Integer type);
}