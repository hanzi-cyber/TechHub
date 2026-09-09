package com.techhub.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置(单机模式)。
 *
 * <p>分布式锁升级:手写 SETNX 锁只有「互斥 + 固定 TTL」,不具备:
 * <ul>
 *   <li><b>可重入</b>:同一线程重复加锁会把自己锁死;</li>
 *   <li><b>锁续期</b>:业务执行超过 TTL,锁被 Redis 过期释放,别的线程能同时进入。</li>
 * </ul>
 * Redisson 的 {@code RLock} 通过 Lua 脚本保证加锁/解锁原子性,内置 watchdog 看门狗:
 * 不指定 leaseTime 时默认 30s 到期,后台每 10s 自动续期,直到显式 unlock。
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 当前 Redis 无密码、用 db0;若启用密码/切换库,追加
        // .setPassword("...").setDatabase(1)
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
