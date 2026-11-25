package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品点赞实体，对应表 {@code product_like}。
 *
 * <p>{@code (userId, productId)} 上有唯一索引，保证一个用户只能赞同一个商品一次。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_like")
public class ProductLike extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 点赞用户 ID */
    private Long userId;

    /** 被点赞的商品 ID */
    private Long productId;
}
