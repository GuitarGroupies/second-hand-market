package com.students.pojo.vo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 VO，用于评论列表展示。
 *
 * @author students
 */
@Data
public class CommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /** 评论 ID */
    private Long id;
    /** 评论者 ID */
    private Long userId;

    /** 评论者昵称 */
    private String nickname;

    /** 评论者头像 */
    private String avatar;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 踩数 */
    private Integer dislikeCount;

    /** 当前登录用户对这条评论的态度：null 未投票，1 已赞，2 已踩 */
    private Integer myVote;

    /** 评论时间 */
    private LocalDateTime createTime;

    /** 子评论（回复） */
    private List<CommentVO> children;


}
