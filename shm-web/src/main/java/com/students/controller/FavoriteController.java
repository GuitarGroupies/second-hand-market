package com.students.controller;

import com.students.common.context.UserContext;
import com.students.common.exception.BizException;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.LikeResultVO;
import com.students.pojo.vo.ProductVO;
import com.students.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品收藏接口。
 *
 * <p>所有接口均需登录——收藏属于私人数据。</p>
 *
 * @author students
 */
@Tag(name = "收藏模块")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏 / 取消收藏商品。
     *
     * @param productId 商品 ID
     * @return 操作后的状态与收藏总数
     */
    @Operation(summary = "收藏/取消收藏商品")
    @PostMapping("/{productId}")
    public Result<LikeResultVO> toggle(@PathVariable Long productId) {
        return Result.success(favoriteService.toggleFavorite(productId, requireLogin()));
    }

    /**
     * 我的收藏列表。
     *
     * @param query 分页参数
     * @return 收藏的商品列表
     */
    @Operation(summary = "我的收藏")
    @GetMapping("/mine")
    public Result<List<ProductVO>> mine(PageQuery query) {
        return Result.success(favoriteService.listMyFavorites(query, requireLogin()));
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
