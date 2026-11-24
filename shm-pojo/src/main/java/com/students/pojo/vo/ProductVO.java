package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表卡片 VO。
 *
 * @author students
 */
@Data
public class ProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 封面图 */
    private String coverImage;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 分类 ID */
    private Long categoryId;

    /** 浏览量 */
    private Integer viewCount;

    /** 发布时间 */
    private LocalDateTime createTime;
}
