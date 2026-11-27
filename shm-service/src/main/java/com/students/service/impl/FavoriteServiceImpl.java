package com.students.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.students.common.exception.BizException;
import com.students.common.result.ResultCode;
import com.students.mapper.ProductMapper;
import com.students.mapper.UserFavoriteMapper;
import com.students.pojo.entity.Product;
import com.students.pojo.entity.UserFavorite;
import com.students.pojo.query.PageQuery;
import com.students.pojo.vo.LikeResultVO;
import com.students.pojo.vo.ProductVO;
import com.students.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品收藏业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;

    private final ProductMapper productMapper;

    @Override
    public LikeResultVO toggleFavorite(Long productId, Long userId) {
        // 1. 商品必须存在
        if (productMapper.selectById(productId) == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 查询是否已收藏
        UserFavorite exist = userFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getProductId, productId));

        boolean favorited;
        if (exist != null) {
            // 已收藏 → 取消（物理删除）
            userFavoriteMapper.physicalDelete(userId, productId);
            favorited = false;
        } else {
            // 未收藏 → 新增
            UserFavorite favorite = new UserFavorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            try {
                userFavoriteMapper.insert(favorite);
            } catch (DuplicateKeyException e) {
                log.warn("重复收藏被唯一索引拦截：userId={}, productId={}", userId, productId);
            }
            favorited = true;
        }

        // 3. 统计该商品的被收藏总数
        Long count = userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getProductId, productId));

        LikeResultVO vo = new LikeResultVO();
        vo.setLiked(favorited);
        vo.setCount(count);
        log.info("收藏切换：userId={}, productId={}, favorited={}, count={}",
                userId, productId, favorited, count);
        return vo;
    }

    @Override
    public List<ProductVO> listMyFavorites(PageQuery query, Long userId) {
        // 1. 分页查收藏记录
        Page<UserFavorite> page = new Page<>(query.getSafePageNum(), query.getSafePageSize());
        Page<UserFavorite> favoritePage = userFavoriteMapper.selectPage(page,
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime));
        List<UserFavorite> records = favoritePage.getRecords();
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量查商品，避免 N+1
        List<Long> productIds = records.stream()
                .map(UserFavorite::getProductId)
                .toList();
        Map<Long, Product> productMap = productMapper.selectByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 3. 按「收藏时间倒序」输出，已被删除的商品跳过
        return records.stream()
                .map(f -> productMap.get(f.getProductId()))
                .filter(p -> p != null)
                .map(this::toProductVO)
                .toList();
    }

    /**
     * 商品实体转列表 VO。
     *
     * @param entity 商品实体
     * @return 列表卡片 VO
     */
    private ProductVO toProductVO(Product entity) {
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setCoverImage(entity.getCoverImage());
        vo.setPrice(entity.getPrice());
        vo.setOriginalPrice(entity.getOriginalPrice());
        vo.setCategoryId(entity.getCategoryId());
        vo.setViewCount(entity.getViewCount());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}