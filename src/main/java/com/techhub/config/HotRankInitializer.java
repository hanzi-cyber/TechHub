package com.techhub.config;

import com.techhub.service.IHotRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动预热:把数据库已有热度刷进 Redis 热度榜。
 * 保证 Redis 重启 / 清空后,首页热门列表不会为空。
 */
@Slf4j
@Component
public class HotRankInitializer implements ApplicationRunner {

    @Autowired
    private IHotRankService hotRankService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            hotRankService.rebuild();
        } catch (Exception e) {
            // 预热失败不阻断启动,查询时会回源 DB 兜底
            log.error("热度榜预热失败", e);
        }
    }
}
