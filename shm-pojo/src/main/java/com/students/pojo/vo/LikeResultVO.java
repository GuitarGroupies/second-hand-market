package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 点赞/想要/评论点赞操作后的结果。
 *
 * <p>前端需要同时知道两件事：当前用户是否处于「已操作」状态，以及最新总数。
 * 只返回总数是不够的——按钮要高亮，必须知道状态。</p>
 *
 * @author students
 */
@Data
public class LikeResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前用户是否已点赞/想要 */
    private Boolean liked;

    /** 最新总数（商品为点赞数，评论为赞数） */
    private Long count;

    /** 踩数（仅评论点赞使用，其余场景为 null） */
    private Long dislikeCount;
}
