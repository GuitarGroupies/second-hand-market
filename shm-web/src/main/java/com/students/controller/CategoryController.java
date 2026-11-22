package com.students.controller;

import com.students.common.result.Result;
import com.students.pojo.vo.CategoryTreeVO;
import com.students.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类接口。
 *
 * @author students
 */
@Tag(name = "商品分类")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 查询两级分类树。
     *
     * @return 分类树
     */
    @Operation(summary = "查询分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.listCategoryTree());
    }
}
