package com.techhub.common.exception;

import com.techhub.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常:业务逻辑校验不通过时抛出,由全局异常处理器统一转为 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
