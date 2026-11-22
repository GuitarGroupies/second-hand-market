package com.students.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回体。
 *
 * <p>所有 Controller 统一返回该结构，前端只需读取 {@code code} 判断成功与否、
 * 读取 {@code data} 获取业务数据，避免每个接口返回格式各不一样。</p>
 *
 * @param <T> 业务数据类型
 * @author students
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码，见 {@link ResultCode} */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据，失败时为 null */
    private T data;

    private Result() {
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功且无返回数据。
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功并携带数据。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败，指定状态码与提示。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败，使用预定义状态码。
     *
     * @param resultCode 状态码枚举
     * @param <T>        数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 是否成功。
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
