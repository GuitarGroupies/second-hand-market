package com.students.common.exception;

import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>把所有异常统一转换成 {@link Result}，前端永远只处理一种响应结构。</p>
 *
 * <p><b>顺序约定：</b>具体异常在前，笼统的 {@link Exception} 兜底放最后。</p>
 *
 * @author students
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：预期内的流程中断，只打 warn，不打印堆栈。
     *
     * @param e 业务异常
     * @return 统一错误结果
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验失败（@Valid 校验 @RequestBody 时触发）。
     *
     * @param e 校验异常
     * @return 统一错误结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 请求路径不存在。
     * <p>Spring Boot 3.2 起，找不到静态资源会抛 NoResourceFoundException，
     * 不单独处理就会被兜底方法当成系统异常，刷出几十行堆栈。</p>
     *
     * @param e 资源不存在异常
     * @return 404 结果
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("请求路径不存在：{}", e.getResourcePath());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 请求方法不支持（例如把 GET 接口用 PUT 调用）。
     *
     * @param e 方法不支持异常
     * @return 统一错误结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(),
                "不支持的请求方法：" + e.getMethod());
    }

    /**
     * 路径或查询参数类型不匹配（例如 /api/product/mine 被当成 id 解析）。
     *
     * @param e 类型不匹配异常
     * @return 统一错误结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配：name={}, value={}", e.getName(), e.getValue());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(),
                "参数格式错误：" + e.getName());
    }

    /**
     * 兜底：未预料的异常。记录完整堆栈，但不返回给前端。
     *
     * @param e 未预期的异常
     * @return 统一错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}