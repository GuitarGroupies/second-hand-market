package com.students.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册入参。
 *
 * <p>只包含前端允许提交的字段，避免用实体直接接收请求时被篡改 id、creditScore 等内部字段。
 * 所有校验注解的 message 会原样返回给前端，由 GlobalExceptionHandler 统一拼接。</p>
 *
 * @author students
 */
@Data
public class UserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名，4-20 位字母数字下划线 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需在 4-20 位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    /** 密码，6-20 位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 位之间")
    private String password;

    /** 昵称，可选，最多 20 位 */
    @Size(max = 20, message = "昵称最多 20 个字符")
    private String nickname;

    /** 手机号，11 位数字 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}