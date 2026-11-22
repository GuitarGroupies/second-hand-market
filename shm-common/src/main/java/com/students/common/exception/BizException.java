package com.students.common.exception;

import com.students.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常。
 *
 * <p>业务规则不满足时抛出（例如「商品不存在」「余额不足」），由全局异常处理器统一
 * 转换为 {@link com.students.common.result.Result} 返回给前端。
 * 区别于系统异常：业务异常是「预期内」的流程分支，日志只记 warn，不打印完整堆栈。</p>
 *
 * @author students
 */
@Getter
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码 */
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.PARAM_ERROR.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
