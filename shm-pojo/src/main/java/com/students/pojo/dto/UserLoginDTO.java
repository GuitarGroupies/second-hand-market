package com.students.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * 用户登录入参。
 *
 * <p>与注册不同，登录时 <b>不校验用户名长度和密码格式</b>，只校验非空。
 * 原因：后续若调整注册规则（例如用户名最短 6 位），老用户的数据不符合新规则，
 * 严格校验会导致这批用户无法登录。</p>
 *
 * @author students
 */
@Data
public class UserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

}
