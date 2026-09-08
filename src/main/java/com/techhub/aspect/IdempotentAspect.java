package com.techhub.aspect;

import com.techhub.annotation.Idempotent;
import com.techhub.common.RedisConstants;
import com.techhub.common.ResultCode;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 幂等切面:对标注 {@code @Idempotent} 的方法,用 Redis SETNX 保证同一幂等标识只放行首次请求。
 * 处理失败(抛异常)时释放 key,便于客户端重试;未携带幂等标识时不拦截。
 * 注意:这是「拒绝重复提交」的简化实现,「成功但响应丢失后重试」会得到「请勿重复提交」,
 * 数据不会重复(这正是幂等要保证的);若要返回上次结果,需额外缓存响应(可后续用 MQ/缓存结果优化)。
 */
@Aspect
@Component
@Order(2)
public class IdempotentAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String idempotentKey = readHeader(idempotent.header());
        if (idempotentKey == null || idempotentKey.isBlank()) {
            // 未携带幂等标识:不强制拦截
            return joinPoint.proceed();
        }

        Long userId = BaseContext.getCurrentId();
        String redisKey = RedisConstants.IDEMPOTENT_KEY_PREFIX
                + (userId != null ? userId : "anonymous") + ":" + idempotentKey;

        Boolean first = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.ttl()));
        if (!Boolean.TRUE.equals(first)) {
            // 同一标识的重复请求
            throw new BusinessException(ResultCode.REPEAT_SUBMIT);
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 处理失败:释放幂等 key,否则客户端用同一 key 重试会被「请勿重复提交」卡住
            stringRedisTemplate.delete(redisKey);
            throw e;
        }
    }

    /** 从当前请求头读取指定 header(无请求上下文时返回 null) */
    private String readHeader(String name) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(name);
    }
}
