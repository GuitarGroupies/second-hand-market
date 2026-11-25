package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品评论实体，对应表 {@code product_comment}。
 *
 * <p>支持两级结构：{@code parentId = 0} 表示一级评论，否则为该条评论的回复。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_comment")
public class ProductComment extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属商品 ID */
    private Long productId;

    /** 评论用户 ID */
    private Long userId;

    /** 父评论 ID，0 表示一级评论 */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 踩数 */
    private Integer dislikeCount;
}
