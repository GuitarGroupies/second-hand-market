package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情 VO。
 *
 * @author students
 */
@Data
public class ProductDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    private Long id;

    /** 卖家 ID */
    private Long sellerId;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 分类 ID */
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 浏览量 */
    private Integer viewCount;

    /** 状态 */
    private Integer status;

    /** 图片列表 */
    private List<String> images;

    /** 点赞总数 */
    private Long likeCount;

    /** 当前用户是否已点赞（未登录为 null） */
    private Boolean liked;

    /** 想要总数 */
    private Long wantCount;

    /** 当前用户是否已想要（未登录为 null） */
    private Boolean wanted;

    /** 发布时间 */
    private LocalDateTime createTime;
}
