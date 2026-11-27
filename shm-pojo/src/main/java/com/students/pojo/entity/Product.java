package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品实体，对应表 {@code product}。
 *
 * <p>金额字段一律使用 {@link BigDecimal}，禁止使用 double/float，
 * 否则会出现 0.1 + 0.2 != 0.3 这类精度问题。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 卖家用户 ID */
    private Long sellerId;

    /** 分类 ID */
    private Long categoryId;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 封面图地址 */
    private String coverImage;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 浏览量 */
    private Integer viewCount;

    /** 状态：0 下架，1 在售，2 已售出 */
    private Integer status;

    /** 标签，逗号分隔，不含 # 号 */
    private String tags;
}
