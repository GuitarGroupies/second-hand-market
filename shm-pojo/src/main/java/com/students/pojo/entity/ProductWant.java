package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品想要实体，对应表 {@code product_want}。
 *
 * <p>表达用户的购买意向，与点赞的区别是：想要的人更接近真实交易意愿。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_want")
public class ProductWant extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 想要的用户 ID */
    private Long userId;

    /** 目标商品 ID */
    private Long productId;
}
