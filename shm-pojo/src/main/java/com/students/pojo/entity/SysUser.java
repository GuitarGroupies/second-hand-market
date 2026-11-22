package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户实体，对应表 {@code sys_user}。
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名，唯一 */
    private String username;

    /** 密码，BCrypt 密文 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 信用分 0-100 */
    private Integer creditScore;

    /** 状态：1 正常，0 禁用 */
    private Integer status;
}
