package com.techhub.service.impl;

import com.techhub.common.RedisConstants;
import com.techhub.common.properties.JwtProperties;
import com.techhub.service.ILoginSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginSessionServiceImpl implements ILoginSessionService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void saveToken(Long userId, String token) {
        String key = RedisConstants.LOGIN_TOKEN_KEY_PREFIX + userId;
        // 过期时间与 JWT 有效期对齐,保证 Redis 会话与 JWT 到期时间一致
        Duration ttl = Duration.ofMillis(jwtProperties.getUserTtl());
        stringRedisTemplate.opsForValue().set(key, token, ttl);
    }

    @Override
    public boolean isValid(Long userId, String token) {
        String cached = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_TOKEN_KEY_PREFIX + userId);
        return cached != null && cached.equals(token);
    }

    @Override
    public void removeToken(Long userId) {
        stringRedisTemplate.delete(RedisConstants.LOGIN_TOKEN_KEY_PREFIX + userId);
    }
}
