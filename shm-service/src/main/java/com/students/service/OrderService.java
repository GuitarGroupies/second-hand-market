package com.students.service;

import com.students.pojo.dto.OrderCreateDTO;
import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.OrderVO;

import java.util.List;

/**
 * 订单业务接口。
 *
 * @author students
 */
public interface OrderService {

    /**
     * 创建订单（下单）。
     *
     * @param dto    下单入参
     * @param userId 买家 ID
     * @return 新订单 ID
     */
    Long create(OrderCreateDTO dto, Long userId);

    /**
     * 支付订单（模拟支付）。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     */
    void pay(Long orderId, Long userId);

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     */
    void cancel(Long orderId, Long userId);

    /**
     * 确认完成（卖家操作）。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     */
    void finish(Long orderId, Long userId);

    /**
     * 我买到的订单列表。
     *
     * @param query  分页参数
     * @param userId 当前用户 ID
     * @return 订单列表
     */
    List<OrderVO> listMyBought(PageQuery query, Long userId);

    /**
     * 我卖出的订单列表。
     *
     * @param query  分页参数
     * @param userId 当前用户 ID
     * @return 订单列表
     */
    List<OrderVO> listMySold(PageQuery query, Long userId);

    /**
     * 取消所有超时未支付的订单（由定时任务调用）。
     *
     * @return 取消的订单数量
     */
    int cancelTimeoutOrders();

    /**
     * 订单详情。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     * @return 订单详情
     */
    OrderVO getDetail(Long orderId, Long userId);
}
