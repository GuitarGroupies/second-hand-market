package com.students.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.students.common.exception.BizException;
import com.students.common.constant.RedisKeyConstant;
import com.students.common.result.ResultCode;
import com.students.mapper.OrderMapper;
import com.students.mapper.ProductMapper;
import com.students.mapper.SysUserMapper;
import com.students.pojo.dto.OrderCreateDTO;
import com.students.pojo.entity.Order;
import com.students.pojo.entity.Product;
import com.students.pojo.entity.SysUser;
import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.OrderVO;
import com.students.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 订单超时时间（分钟），超时未支付自动取消 */
    private static final long ORDER_TIMEOUT_MINUTES = 15L;

    /** 商品状态：在售 */
    private static final int PRODUCT_ON_SALE = 1;

    /** 商品状态：已售出 */
    private static final int PRODUCT_SOLD = 2;

    private final OrderMapper orderMapper;

    private final ProductMapper productMapper;

    private final SysUserMapper sysUserMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateDTO dto, Long userId) {
        Long productId = dto.getProductId();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        // 1. 不能买自己的商品
        if (product.getSellerId().equals(userId)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "不能购买自己发布的商品");
        }
        // 2. 商品必须在售
        if (product.getStatus() != PRODUCT_ON_SALE) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该商品已下架或已售出");
        }
        // 3. 防止重复下单：同一买家对该商品已有待支付订单
        Long existing = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId)
                .eq(Order::getProductId, productId)
                .eq(Order::getStatus, Order.STATUS_UNPAID));
        if (existing != null && existing > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "你已经有一个待支付的订单，请先完成支付或取消");
        }

        // 4. 防超卖：条件更新，只有 stock >= 1 才会减成功
        //    为什么不用「查出来再减」：并发下两个请求都读到 stock=1，会各减一次
        LambdaUpdateWrapper<Product> update = new LambdaUpdateWrapper<>();
        update.setSql("stock = stock - 1")
                .eq(Product::getId, productId)
                .eq(Product::getStatus, PRODUCT_ON_SALE)
                .ge(Product::getStock, 1);
        int affected = productMapper.update(null, update);
        if (affected == 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "商品库存不足，下单失败");
        }

        // 5. 落库，金额取自商品价格，绝不使用前端传入值
        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(userId);
        order.setSellerId(product.getSellerId());
        order.setProductId(productId);
        order.setProductTitle(product.getTitle());
        order.setAmount(product.getPrice());
        order.setStatus(Order.STATUS_UNPAID);
        orderMapper.insert(order);

        log.info("下单成功：orderId={}, orderNo={}, buyerId={}, productId={}, amount={}",
                order.getId(), order.getOrderNo(), userId, productId, product.getPrice());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId, true);
        if (order.getStatus() != Order.STATUS_UNPAID) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "订单状态不是待支付，无法支付");
        }
        // 模拟支付成功
        order.setStatus(Order.STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 商品标记为已售出
        Product product = new Product();
        product.setId(order.getProductId());
        product.setStatus(PRODUCT_SOLD);
        productMapper.updateById(product);

        log.info("支付成功：orderId={}, buyerId={}", orderId, userId);
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + order.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId, true);
        if (order.getStatus() != Order.STATUS_UNPAID) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "只有待支付的订单才能取消");
        }
        order.setStatus(Order.STATUS_CANCELED);
        orderMapper.updateById(order);

        // 归还库存，并把商品恢复到在售
        LambdaUpdateWrapper<Product> update = new LambdaUpdateWrapper<>();
        update.setSql("stock = stock + 1")
                .set(Product::getStatus, PRODUCT_ON_SALE)
                .eq(Product::getId, order.getProductId());
        productMapper.update(null, update);

        log.info("取消订单：orderId={}, userId={}", orderId, userId);
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + order.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId, false);
        if (order.getStatus() != Order.STATUS_PAID) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "只有已支付的订单才能完成");
        }
        order.setStatus(Order.STATUS_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单完成：orderId={}, userId={}", orderId, userId);
    }

    @Override
    public List<OrderVO> listMyBought(PageQuery query, Long userId) {
        return queryOrders(query, Order::getBuyerId, userId);
    }

    @Override
    public List<OrderVO> listMySold(PageQuery query, Long userId) {
        return queryOrders(query, Order::getSellerId, userId);
    }

    @Override
    public OrderVO getDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        // 只有买卖双方能查看
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "无权查看该订单");
        }
        return toVOList(List.of(order)).get(0);
    }

    /**
     * 通用订单分页查询。
     *
     * @param query    分页参数
     * @param columnFn 关联列（买家或卖家）
     * @param userId   用户 ID
     * @return 订单列表
     */
    private List<OrderVO> queryOrders(PageQuery query,
                                      com.baomidou.mybatisplus.core.toolkit.support.SFunction<Order, Long> columnFn,
                                      Long userId) {
        Page<Order> page = new Page<>(query.getSafePageNum(), query.getSafePageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(columnFn, userId)
                .orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        return toVOList(result.getRecords());
    }

    /**
     * 批量组装订单 VO，避免逐条查用户信息造成 N+1。
     *
     * @param orders 订单实体列表
     * @return 订单 VO 列表
     */
    private List<OrderVO> toVOList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        // 收集买家、卖家、商品 ID，各查一次
        Set<Long> userIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Order o : orders) {
            userIds.add(o.getBuyerId());
            userIds.add(o.getSellerId());
            productIds.add(o.getProductId());
        }
        Map<Long, SysUser> userMap = sysUserMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, Product> productMap = productMapper.selectByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setProductId(o.getProductId());
            vo.setProductTitle(o.getProductTitle());
            vo.setAmount(o.getAmount());
            vo.setStatus(o.getStatus());
            vo.setStatusText(statusText(o.getStatus()));
            vo.setBuyerId(o.getBuyerId());
            vo.setSellerId(o.getSellerId());
            vo.setCreateTime(o.getCreateTime());
            vo.setPayTime(o.getPayTime());
            SysUser buyer = userMap.get(o.getBuyerId());
            SysUser seller = userMap.get(o.getSellerId());
            vo.setBuyerNickname(buyer == null ? "" : buyer.getNickname());
            vo.setSellerNickname(seller == null ? "" : seller.getNickname());
            Product p = productMap.get(o.getProductId());
            vo.setProductCover(p == null ? "" : p.getCoverImage());
            return vo;
        }).toList();
    }

    /**
     * 订单状态转中文。
     *
     * @param status 状态值
     * @return 中文描述
     */
    private String statusText(Integer status) {
        if (Objects.isNull(status)) {
            return "未知";
        }
        return switch (status) {
            case Order.STATUS_UNPAID -> "待支付";
            case Order.STATUS_PAID -> "已支付";
            case Order.STATUS_FINISHED -> "已完成";
            case Order.STATUS_CANCELED -> "已取消";
            default -> "未知";
        };
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeoutOrders() {
        // 找出 15 分钟前创建、仍未支付的订单
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        List<Order> timeoutOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, Order.STATUS_UNPAID)
                .lt(Order::getCreateTime, deadline));
        if (timeoutOrders.isEmpty()) {
            return 0;
        }
        for (Order order : timeoutOrders) {
            order.setStatus(Order.STATUS_CANCELED);
            orderMapper.updateById(order);
            // 归还库存并恢复在售
            LambdaUpdateWrapper<Product> update = new LambdaUpdateWrapper<>();
            update.setSql("stock = stock + 1")
                    .set(Product::getStatus, PRODUCT_ON_SALE)
                    .eq(Product::getId, order.getProductId());
            productMapper.update(null, update);
            stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + order.getProductId());
        }
        log.info("超时订单自动取消：{} 笔", timeoutOrders.size());
        return timeoutOrders.size();
    }

    /**
     * 取订单并校验归属。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     * @param asBuyer true 表示只允许买家操作，false 表示只允许卖家操作
     * @return 订单实体
     */
    private Order getOwnedOrder(Long orderId, Long userId, boolean asBuyer) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        Long ownerId = asBuyer ? order.getBuyerId() : order.getSellerId();
        if (!ownerId.equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(),
                    asBuyer ? "只能操作自己的订单" : "只有卖家可以操作该订单");
        }
        return order;
    }
}
