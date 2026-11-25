package com.students.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.students.common.exception.BizException;
import com.students.common.result.ResultCode;
import com.students.mapper.CommentLikeMapper;
import com.students.mapper.ProductCommentMapper;
import com.students.mapper.ProductMapper;
import com.students.mapper.SysUserMapper;
import com.students.pojo.dto.CommentCreateDTO;
import com.students.pojo.entity.CommentLike;
import com.students.pojo.entity.ProductComment;
import com.students.pojo.entity.SysUser;
import com.students.pojo.vo.CommentVO;
import com.students.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    /** 一级评论的 parentId */
    private static final long ROOT_PARENT_ID = 0L;

    private final ProductCommentMapper productCommentMapper;

    private final ProductMapper productMapper;

    private final SysUserMapper sysUserMapper;

    private final CommentLikeMapper commentLikeMapper;

    @Override
    public Long create(Long productId, CommentCreateDTO dto, Long userId) {
        // 1. 商品必须存在
        if (productMapper.selectById(productId) == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 如果是回复，父评论必须存在且属于同一商品，且必须是一级评论（只支持两级）
        Long parentId = dto.getParentId() == null ? ROOT_PARENT_ID : dto.getParentId();
        if (parentId != ROOT_PARENT_ID) {
            ProductComment parent = productCommentMapper.selectById(parentId);
            if (parent == null) {
                throw new BizException(ResultCode.COMMENT_NOT_FOUND);
            }
            if (!parent.getProductId().equals(productId)) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "父评论不属于该商品");
            }
            if (parent.getParentId() != ROOT_PARENT_ID) {
                // 只支持两级：回复只能挂在一级评论下
                parentId = parent.getParentId();
            }
        }

        // 3. 落库
        ProductComment comment = new ProductComment();
        comment.setProductId(productId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(dto.getContent().trim());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        productCommentMapper.insert(comment);

        log.info("发表评论成功：commentId={}, productId={}, userId={}, parentId={}",
                comment.getId(), productId, userId, parentId);
        return comment.getId();
    }

    @Override
    public List<CommentVO> listByProduct(Long productId, Long userId) {
        // 1. 一次查出该商品下所有评论（含回复），按时间升序
        List<ProductComment> all = productCommentMapper.selectList(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getProductId, productId)
                        .orderByAsc(ProductComment::getCreateTime));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量查评论者信息，避免 N+1（N 条评论发 N 次查询）
        Set<Long> userIds = all.stream()
                .map(ProductComment::getUserId)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 3. 批量查当前用户对这些评论的投票，避免 N+1
        Map<Long, Integer> voteMap;
        if (userId == null) {
            // 游客：不查投票
            voteMap = Collections.emptyMap();
        } else {
            List<Long> commentIds = all.stream()
                    .map(ProductComment::getId)
                    .collect(Collectors.toList());
            voteMap = commentLikeMapper.selectList(
                            new LambdaQueryWrapper<CommentLike>()
                                    .eq(CommentLike::getUserId, userId)
                                    .in(CommentLike::getCommentId, commentIds))
                    .stream()
                    .collect(Collectors.toMap(CommentLike::getCommentId, CommentLike::getType));
        }

        // 4. 先转 VO，同时建立 id → VO 的下标
        Map<Long, CommentVO> voMap = new java.util.HashMap<>(all.size());
        for (ProductComment c : all) {
            voMap.put(c.getId(), toVO(c, userMap, voteMap));
        }

        // 5. 组装两级树：回复挂到父评论的 children 上
        List<CommentVO> roots = new ArrayList<>(16);
        for (ProductComment c : all) {
            CommentVO vo = voMap.get(c.getId());
            if (c.getParentId() == null || c.getParentId() == ROOT_PARENT_ID) {
                roots.add(vo);
            } else {
                CommentVO parentVo = voMap.get(c.getParentId());
                if (parentVo != null) {
                    parentVo.getChildren().add(vo);
                } else {
                    // 父评论被删了，孤儿评论降级为一级
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    /**
     * 单个评论实体转 VO。
     *
     * @param entity  评论实体
     * @param userMap 用户信息缓存：userId → SysUser
     * @param voteMap 投票缓存：commentId → type
     * @return 评论 VO
     */
    private CommentVO toVO(ProductComment entity,
                           Map<Long, SysUser> userMap,
                           Map<Long, Integer> voteMap) {
        CommentVO vo = new CommentVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setContent(entity.getContent());
        vo.setLikeCount(entity.getLikeCount());
        vo.setDislikeCount(entity.getDislikeCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setMyVote(voteMap.get(entity.getId()));
        // children 必须是空集合而不是 null，前端可以直接 push
        vo.setChildren(new ArrayList<>(4));

        SysUser user = userMap.get(entity.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
}