package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.dto.CommentCreateDTO;
import com.students.pojo.vo.CommentVO;
import com.students.service.CommentService;
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
 * 商品评论接口。
 *
 * @author students
 */
@Tag(name = "评论模块")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论或回复（需登录）。
     *
     * @param productId 商品 ID
     * @param dto       评论内容
     * @return 新评论 ID
     */
    @Operation(summary = "发表评论/回复")
    @PostMapping("/product/{productId}")
    public Result<Long> create(@PathVariable Long productId,
                               @Valid @RequestBody CommentCreateDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(commentService.create(productId, dto, userId));
    }

    /**
     * 查询商品评论列表（游客可看）。
     *
     * @param productId 商品 ID
     * @return 两级评论树
     */
    @Operation(summary = "商品评论列表")
    @GetMapping("/product/{productId}")
    public Result<List<CommentVO>> list(@PathVariable Long productId) {
        // 游客也能浏览评论，未登录时 userId 为 null
        Long userId = UserContext.getUserId();
        return Result.success(commentService.listByProduct(productId, userId));
    }
}