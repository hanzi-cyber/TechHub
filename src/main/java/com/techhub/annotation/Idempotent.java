package com.techhub.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等:标注在 Controller 方法上,由 {@code IdempotentAspect} 通过 Redis SETNX 防重复提交。
 * 客户端需在请求头带上幂等标识(默认 Idempotent-Key,如 UUID),同一标识只放行首次请求。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /** 携带幂等标识的请求头名 */
    String header() default "Idempotent-Key";

    /** 幂等 key 有效期(秒),防止 key 永久占用内存 */
    long ttl() default 300;
}
