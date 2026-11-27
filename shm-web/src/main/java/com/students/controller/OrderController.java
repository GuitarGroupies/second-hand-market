package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.dto.OrderCreateDTO;
import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.OrderVO;
import com.students.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口。
 *
 * <p>所有接口均需登录。</p>
 *
 * @author students
 */
@Tag(name = "订单模块")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单。
     *
     * @param dto 下单入参
     * @return 新订单 ID
     */
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto, requireLogin()));
    }

    /**
     * 支付订单（模拟）。
     *
     * @param id 订单 ID
     * @return 空结果
     */
    @Operation(summary = "支付订单")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id, requireLogin());
        return Result.success();
    }

    /**
     * 取消订单。
     *
     * @param id 订单 ID
     * @return 空结果
     */
    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, requireLogin());
        return Result.success();
    }

    /**
     * 确认完成（卖家操作）。
     *
     * @param id 订单 ID
     * @return 空结果
     */
    @Operation(summary = "确认订单完成")
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        orderService.finish(id, requireLogin());
        return Result.success();
    }

    /**
     * 我买到的订单。
     *
     * @param query 分页参数
     * @return 订单列表
     */
    @Operation(summary = "我买到的")
    @GetMapping("/bought")
    public Result<List<OrderVO>> bought(PageQuery query) {
        return Result.success(orderService.listMyBought(query, requireLogin()));
    }

    /**
     * 我卖出的订单。
     *
     * @param query 分页参数
     * @return 订单列表
     */
    @Operation(summary = "我卖出的")
    @GetMapping("/sold")
    public Result<List<OrderVO>> sold(PageQuery query) {
        return Result.success(orderService.listMySold(query, requireLogin()));
    }

    /**
     * 订单详情。
     *
     * @param id 订单 ID
     * @return 订单详情
     */
    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id, requireLogin()));
    }

    /**
     * 取当前登录用户 ID，未登录直接抛异常。
     *
     * @return 当前用户 ID
     */
    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
