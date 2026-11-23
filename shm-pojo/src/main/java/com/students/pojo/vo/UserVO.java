package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息出参，用于返回给前端。
 *
 * <p><b>为什么不直接返回 SysUser 实体：</b>实体里有 password、deleted、createTime
 * 等内部字段，直接返回会把密码哈希泄露给前端，也会让接口与表结构强绑定。</p>
 *
 * @author students
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 信用分 */
    private Integer creditScore;
}
