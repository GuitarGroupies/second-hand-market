package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 评论点赞踩实体，对应表 {@code comment_like}。
 *
 * <p>一个用户对同一条评论只能投一票（赞或踩），由唯一索引保证。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_like")
public class CommentLike extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型：赞 */
    public static final int TYPE_LIKE = 1;

    /** 类型：踩 */
    public static final int TYPE_DISLIKE = 2;

    /** 操作用户 ID */
    private Long userId;

    /** 评论 ID */
    private Long commentId;

    /** 类型：1 赞，2 踩 */
    private Integer type;
}
