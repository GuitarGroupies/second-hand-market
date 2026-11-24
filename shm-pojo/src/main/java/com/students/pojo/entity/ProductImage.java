package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品图片实体，对应表 {@code product_image}。
 *
 * <p>一个商品可以有多张图片，用 productId 关联，sort 控制展示顺序。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_image")
public class ProductImage extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属商品 ID */
    private Long productId;

    /** 图片地址 */
    private String url;

    /** 排序值，越小越靠前 */
    private Integer sort;
}