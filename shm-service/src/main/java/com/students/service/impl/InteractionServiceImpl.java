package com.students.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.students.common.exception.BizException;
import com.students.common.result.ResultCode;
import com.students.mapper.CommentLikeMapper;
import com.students.mapper.ProductCommentMapper;
import com.students.mapper.ProductLikeMapper;
import com.students.mapper.ProductWantMapper;
import com.students.mapper.ProductMapper;
import com.students.pojo.entity.CommentLike;
import com.students.pojo.entity.ProductComment;
import com.students.pojo.entity.ProductLike;
import com.students.pojo.entity.ProductWant;
import com.students.pojo.vo.LikeResultVO;
import com.students.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 互动业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final ProductLikeMapper productLikeMapper;

    private final ProductWantMapper productWantMapper;

    private final ProductMapper productMapper;

    private final ProductCommentMapper productCommentMapper;

    private final CommentLikeMapper commentLikeMapper;

    @Override
    public LikeResultVO toggleProductLike(Long productId, Long userId) {
        // 1. 校验商品是否存在
        if (productMapper.selectById(productId) == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 查询是否已点赞
        ProductLike exist = productLikeMapper.selectOne(
                new LambdaQueryWrapper<ProductLike>()
                        .eq(ProductLike::getUserId, userId)
                        .eq(ProductLike::getProductId, productId));

        boolean liked;
        if (exist != null) {
            // 3. 已点赞 → 取消（物理删除，避免占用唯一索引导致再次点赞插入失败）
            productLikeMapper.physicalDelete(userId, productId);
            liked = false;
        } else {
            // 4. 未点赞 → 新增
            ProductLike like = new ProductLike();
            like.setUserId(userId);
            like.setProductId(productId);
            try {
                productLikeMapper.insert(like);
            } catch (DuplicateKeyException e) {
                // 并发下另一个请求已经插入，唯一索引兜底；这里视为已点赞
                log.warn("重复点赞被唯一索引拦截：userId={}, productId={}", userId, productId);
            }
            liked = true;
        }

        // 5. 统计最新总数
        Long count = productLikeMapper.selectCount(
                new LambdaQueryWrapper<ProductLike>()
                        .eq(ProductLike::getProductId, productId));

        LikeResultVO vo = new LikeResultVO();
        vo.setLiked(liked);
        vo.setCount(count);
        log.info("商品点赞切换：userId={}, productId={}, liked={}, count={}",
                userId, productId, liked, count);
        return vo;
    }

    @Override
    public LikeResultVO toggleProductWant(Long productId, Long userId) {
        // 1. 校验商品是否存在
        if (productMapper.selectById(productId) == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 查询是否已想要
        ProductWant exist = productWantMapper.selectOne(
                new LambdaQueryWrapper<ProductWant>()
                        .eq(ProductWant::getUserId, userId)
                        .eq(ProductWant::getProductId, productId));

        boolean wanted;
        if (exist != null) {
            // 3. 已想要 → 取消（物理删除，避免占用唯一索引）
            productWantMapper.physicalDelete(userId, productId);
            wanted = false;
        } else {
            // 4. 未想要 → 新增
            ProductWant want = new ProductWant();
            want.setUserId(userId);
            want.setProductId(productId);
            try {
                productWantMapper.insert(want);
            } catch (DuplicateKeyException e) {
                log.warn("重复想要被唯一索引拦截：userId={}, productId={}", userId, productId);
            }
            wanted = true;
        }

        // 5. 统计最新总数
        Long count = productWantMapper.selectCount(
                new LambdaQueryWrapper<ProductWant>()
                        .eq(ProductWant::getProductId, productId));

        LikeResultVO vo = new LikeResultVO();
        vo.setLiked(wanted);
        vo.setCount(count);
        log.info("商品想要切换：userId={}, productId={}, wanted={}, count={}",
                userId, productId, wanted, count);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO toggleCommentLike(Long commentId, Long userId, Integer type) {
        // 0. 参数校验：只接受 1 赞 / 2 踩
        if (type == null || (type != CommentLike.TYPE_LIKE && type != CommentLike.TYPE_DISLIKE)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "操作类型只能为 1(赞) 或 2(踩)");
        }

        // 1. 校验评论是否存在
        if (productCommentMapper.selectById(commentId) == null) {
            throw new BizException(ResultCode.COMMENT_NOT_FOUND);
        }

        // 2. 查询当前用户对该评论的投票（赞或踩，最多一条）
        CommentLike exist = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, userId)
                        .eq(CommentLike::getCommentId, commentId));

        boolean likedNow;
        if (exist == null) {
            // 分支一：没投过票 → 新增
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            like.setType(type);
            commentLikeMapper.insert(like);
            productCommentMapper.incrCount(commentId, columnOf(type), 1);
            likedNow = type == CommentLike.TYPE_LIKE;
        } else if (exist.getType().equals(type)) {
            // 分支二：点的还是原来那个 → 取消
            commentLikeMapper.physicalDelete(userId, commentId);
            productCommentMapper.incrCount(commentId, columnOf(type), -1);
            likedNow = false;
        } else {
            // 分支三：赞踩互换 → 改 type，两个计数各动一次
            commentLikeMapper.switchType(exist.getId(), type);
            productCommentMapper.incrCount(commentId, columnOf(exist.getType()), -1);
            productCommentMapper.incrCount(commentId, columnOf(type), 1);
            likedNow = type == CommentLike.TYPE_LIKE;
        }

        // 3. 取出最新计数返回
        ProductComment latest = productCommentMapper.selectById(commentId);
        LikeResultVO vo = new LikeResultVO();
        vo.setLiked(likedNow);
        vo.setCount(latest.getLikeCount().longValue());
        vo.setDislikeCount(latest.getDislikeCount().longValue());
        log.info("评论投票：userId={}, commentId={}, type={}, liked={}, likeCount={}, dislikeCount={}",
                userId, commentId, type, likedNow, vo.getCount(), vo.getDislikeCount());
        return vo;
    }

    /**
     * 把投票类型映射成数据库列名。
     *
     * <p>列名会拼进 incrCount 的 ${} 中，因此只能由代码内部决定，
     * 绝不能接受前端传入的字符串。</p>
     *
     * @param type 投票类型
     * @return 列名
     */
    private String columnOf(Integer type) {
        return type == CommentLike.TYPE_LIKE ? "like_count" : "dislike_count";
    }
}