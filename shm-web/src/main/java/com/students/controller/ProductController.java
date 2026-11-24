package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.dto.ProductCreateDTO;
import com.students.pojo.query.ProductQuery;
import com.students.pojo.vo.ProductDetailVO;
import com.students.pojo.vo.ProductVO;
import com.students.service.ProductService;
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
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(productService.create(dto, userId));
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
     * 商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }
}
