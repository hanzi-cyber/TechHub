package com.techhub.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流:标注在 Controller 方法上,由 {@code RateLimitAspect} 基于 Redis 计数实现固定窗口限流。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 时间窗口内允许的最大请求次数 */
    int limit() default 10;

    /** 时间窗口长度(秒) */
    int window() default 60;
}
