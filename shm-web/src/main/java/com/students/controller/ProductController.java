package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.dto.ProductCreateDTO;
import com.students.pojo.dto.ProductUpdateDTO;
import com.students.pojo.query.ProductQuery;
import com.students.pojo.vo.ProductDetailVO;
import com.students.pojo.vo.ProductVO;
import com.students.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品接口。
 *
 * @author students
 */
@Tag(name = "商品模块")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 发布商品。
     *
     * @param dto 发布入参
     * @return 新商品 ID
     */
    @Operation(summary = "发布商品")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.create(dto, requireLogin()));
    }

    /**
     * 商品列表。
     *
     * @param query 查询条件
     * @return 商品列表
     */
    @Operation(summary = "商品列表")
    @GetMapping("/list")
    public Result<List<ProductVO>> list(ProductQuery query) {
        return Result.success(productService.listProducts(query));
    }

    /**
     * 我发布的商品列表。
     *
     * <p>注意：该映射必须写在 {@code /{id}} 之前，
     * 否则路径变量会优先解析 "mine"，导致 Long 转换失败。</p>
     *
     * @param query 查询条件
     * @return 商品列表
     */
    @Operation(summary = "我的发布")
    @GetMapping("/mine")
    public Result<List<ProductVO>> mine(ProductQuery query) {
        return Result.success(productService.listMine(query, requireLogin()));
    }

    /**
     * 商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id, UserContext.getUserId()));
    }

    /**
     * 修改商品（部分更新，字段为 null 表示不修改）。
     *
     * @param id  商品 ID
     * @param dto 修改内容
     * @return 空结果
     */
    @Operation(summary = "修改商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody ProductUpdateDTO dto) {
        productService.update(id, dto, requireLogin());
        return Result.success();
    }

    /**
     * 上架或下架商品。
     *
     * @param id     商品 ID
     * @param status 目标状态：0 下架，1 在售，2 已售出
     * @return 空结果
     */
    @Operation(summary = "上架/下架商品")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        productService.updateStatus(id, status, requireLogin());
        return Result.success();
    }

    /**
     * 删除商品。
     *
     * @param id 商品 ID
     * @return 空结果
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id, requireLogin());
        return Result.success();
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
