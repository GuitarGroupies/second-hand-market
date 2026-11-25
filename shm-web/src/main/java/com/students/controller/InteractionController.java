package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.vo.LikeResultVO;
import com.students.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 互动接口：商品点赞、想要。
 *
 * <p>所有接口都要求登录——未登录用户无法点赞或表达想要。</p>
 *
 * @author students
 */
@Tag(name = "互动模块")
@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    /**
     * 切换商品点赞状态。
     *
     * @param productId 商品 ID
     * @return 操作后的状态与总数
     */
    @Operation(summary = "点赞/取消点赞商品")
    @PostMapping("/product/{productId}/like")
    public Result<LikeResultVO> toggleLike(@PathVariable Long productId) {
        Long userId = currentUserId();
        return Result.success(interactionService.toggleProductLike(productId, userId));
    }

    /**
     * 切换商品想要状态。
     *
     * @param productId 商品 ID
     * @return 操作后的状态与总数
     */
    @Operation(summary = "想要/取消想要商品")
    @PostMapping("/product/{productId}/want")
    public Result<LikeResultVO> toggleWant(@PathVariable Long productId) {
        Long userId = currentUserId();
        return Result.success(interactionService.toggleProductWant(productId, userId));
    }

    /**
     * 取当前登录用户 ID，未登录直接抛异常。
     *
     * <p>多个方法都需要这段判断，抽成私有方法避免重复。
     * 后续接口多了可以改成自定义注解 + 拦截器统一处理。</p>
     *
     * @return 当前用户 ID
     */
    /**
     * 对评论点赞或踩。
     *
     * <p>赞和踩互斥：给已赞的评论点踩，会自动撤销赞并记为踩。</p>
     *
     * @param commentId 评论 ID
     * @param type      1 赞，2 踩
     * @return 操作后的赞数与踩数
     */
    @Operation(summary = "评论点赞/踩")
    @PostMapping("/comment/{commentId}/like")
    public Result<LikeResultVO> toggleCommentLike(@PathVariable Long commentId,
                                                  @RequestParam Integer type) {
        Long userId = currentUserId();
        return Result.success(interactionService.toggleCommentLike(commentId, userId, type));
    }
    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}