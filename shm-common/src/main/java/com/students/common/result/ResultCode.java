package com.students.common.result;

import lombok.Getter;

/**
 * 统一状态码枚举。
 *
 * <p>编码规则：200 表示成功；4xxxx 表示客户端 / 业务错误；5xxxx 表示服务端错误。
 * 细分码采用「段位 + 三位序号」，例如 40401 表示 404 段的第 1 个业务子错误，
 * 这样前端可以按段位统一处理，也便于排查问题。</p>
 *
 * @author students
 */
@Getter
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数不合法 */
    PARAM_ERROR(40001, "请求参数错误"),

    /** 未登录或登录态已失效 */
    UNAUTHORIZED(40101, "未登录或登录已过期"),

    /** 没有访问权限 */
    FORBIDDEN(40301, "无权限访问"),

    /** 资源不存在 */
    NOT_FOUND(40401, "请求的资源不存在"),

    /** 分类不存在 */
    CATEGORY_NOT_FOUND(40402, "分类不存在"),

    /** 商品不存在 */
    PRODUCT_NOT_FOUND(40403, "商品不存在"),

    /** 系统内部异常 */
    SYSTEM_ERROR(50000, "系统繁忙，请稍后再试");

    /** 状态码 */
    private final Integer code;

    /** 提示信息 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
