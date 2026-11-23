package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功出参。
 *
 * <p>前端拿到 token 后需要保存（一般是 localStorage），并在后续每个请求的
 * Header 中带上 {@code Authorization: Bearer <token>}；同时返回用户基本信息，
 * 前端可以直接渲染头像和昵称，省一次请求。</p>
 *
 * @author students
 */
@Data
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录令牌，前端需在后续请求中携带 */
    private String token;

    /** 登录用户基本信息 */
    private UserVO user;
}