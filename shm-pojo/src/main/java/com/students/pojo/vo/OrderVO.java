package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 VO。
 *
 * @author students
 */
@Data
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 订单 ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 商品 ID */
    private Long productId;

    /** 商品标题快照 */
    private String productTitle;

    /** 商品封面 */
    private String productCover;

    /** 订单金额 */
    private BigDecimal amount;

    /** 状态值 */
    private Integer status;

    /** 状态文字，由后端统一给出，前端直接展示 */
    private String statusText;

    /** 买家 ID */
    private Long buyerId;

    /** 买家昵称 */
    private String buyerNickname;

    /** 卖家 ID */
    private Long sellerId;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime payTime;
}
