package com.students.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发表评论入参。
 *
 * @author students
 */
@Data
public class CommentCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 父评论 ID，0 或 null 表示一级评论 */
    private Long parentId;
    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论最多 500 个字符")
    private String content;

}
