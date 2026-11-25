package com.students.service;

import com.students.pojo.dto.CommentCreateDTO;
import com.students.pojo.vo.CommentVO;

import java.util.List;

/**
 * 评论业务接口。
 *
 * @author students
 */
public interface CommentService {

    /**
     * 发表评论或回复。
     *
     * @param productId 商品 ID
     * @param dto       评论内容
     * @param userId    当前用户 ID
     * @return 新评论 ID
     */
    Long create(Long productId, CommentCreateDTO dto, Long userId);

    /**
     * 查询商品下的评论列表（两级结构）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID，可为 null（游客）
     * @return 一级评论列表，每条含其回复
     */
    List<CommentVO> listByProduct(Long productId, Long userId);
}