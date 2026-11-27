package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品收藏实体，对应表 {@code user_favorite}。
 *
 * <p>与点赞的区别在语义：点赞是「喜欢」，收藏是「想买 / 先存着」，
 * 收藏列表在个人中心可见。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_favorite")
public class UserFavorite extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 商品 ID */
    private Long productId;
}