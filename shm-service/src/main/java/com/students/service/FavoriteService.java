package com.students.service;

import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.LikeResultVO;
import com.students.pojo.vo.ProductVO;

import java.util.List;

/**
 * 商品收藏业务接口。
 *
 * @author students
 */
public interface FavoriteService {

    /**
     * 切换收藏状态（已收藏则取消）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID
     * @return 操作后的状态与收藏总数
     */
    LikeResultVO toggleFavorite(Long productId, Long userId);

    /**
     * 查询我的收藏列表。
     *
     * <p>返回的是商品信息而非收藏记录——用户要看的是商品卡片。</p>
     *
     * @param query  分页参数
     * @param userId 当前用户 ID
     * @return 商品列表
     */
    List<ProductVO> listMyFavorites(PageQuery query, Long userId);
}