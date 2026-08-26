package com.techhub.common.exception;

import com.techhub.common.Result;
import com.techhub.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器:所有异常统一转成 Result,避免把堆栈暴露给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常(@RequestBody 的 @Valid, 以及表单绑定)
     * 注意:MethodArgumentNotValidException 是 BindException 的子类,这里统一处理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleValidException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 参数校验异常(@RequestParam / @PathVariable 上的 @Validated 校验)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
