package com.students.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建订单入参。
 *
 * <p><b>安全要点：</b>金额不接收前端传值，由后端按商品实时价格计算，
 * 否则用户可以伪造 amount 用 1 分钱买走商品。</p>
 *
 * @author students
 */
@Data
public class OrderCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 要购买的商品 ID */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;
}
