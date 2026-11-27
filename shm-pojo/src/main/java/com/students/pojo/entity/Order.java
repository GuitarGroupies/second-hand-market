package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应表 orders。
 *
 * <p>状态流转：0 待支付 → 1 已支付 → 2 已完成；待支付可取消为 3。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态：待支付 */
    public static final int STATUS_UNPAID = 0;

    /** 状态：已支付 */
    public static final int STATUS_PAID = 1;

    /** 状态：已完成 */
    public static final int STATUS_FINISHED = 2;

    /** 状态：已取消 */
    public static final int STATUS_CANCELED = 3;

    /** 订单号，业务唯一 */
    private String orderNo;

    /** 买家 ID */
    private Long buyerId;

    /** 卖家 ID */
    private Long sellerId;

    /** 商品 ID */
    private Long productId;

    /** 下单时的商品标题快照 */
    private String productTitle;

    /** 订单金额 */
    private BigDecimal amount;

    /** 状态：0 待支付，1 已支付，2 已完成，3 已取消 */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 完成时间 */
    private LocalDateTime finishTime;
}
