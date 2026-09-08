package com.techhub.aspect;

import com.techhub.annotation.RateLimit;
import com.techhub.common.RedisConstants;
import com.techhub.common.ResultCode;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 限流切面:对标注 {@code @RateLimit} 的方法,基于 Redis 计数实现固定窗口限流。
 * 用 Lua 脚本保证「计数 +1 与首次设置过期时间」的原子性,避免并发下 TTL 丢失导致 key 永不释放。
 * 优先级最高(先于幂等切面执行):先限流挡掉超频请求,避免无谓占用幂等 key。
 */
@Aspect
@Component
@Order(1)
public class RateLimitAspect {

    /** 原子执行 INCR + 首次 EXPIRE,返回当前窗口计数 */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('incr', KEYS[1]) " +
            "if current == 1 then redis.call('expire', KEYS[1], ARGV[1]) end " +
            "return current",
            Long.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 限流维度:登录用户按 userId(这些接口都要登录,理论上有值)
        Long userId = BaseContext.getCurrentId();
        String identity = userId != null ? String.valueOf(userId) : "anonymous";
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = RedisConstants.RATE_LIMIT_KEY_PREFIX
                + signature.getDeclaringType().getSimpleName()
                + ":" + signature.getMethod().getName()
                + ":" + identity;

        Long current = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(rateLimit.window()));

        if (current != null && current > rateLimit.limit()) {
            throw new BusinessException(ResultCode.RATE_LIMITED);
        }
        return joinPoint.proceed();
    }
}
