package com.techhub.task;

import com.techhub.service.IHotRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务:把 Redis 热度榜(ZSET)的热度分回写到 t_post.hot_score。
 * 目的:让「带标签/关键词筛选的热门排序」也能走 DB 的 hot_score,与纯热门列表(ZSET)保持同一个真相。
 */
@Slf4j
@Component
public class HotScoreSyncTask {

    @Autowired
    private IHotRankService hotRankService;

    /** 每 5 分钟执行一次(生产可改为配置驱动) */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncHotScore() {
        try {
            hotRankService.syncScoresToDb();
        } catch (Exception e) {
            // 回写失败不阻断后续调度,下个周期重试
            log.error("hot_score 定时回写失败", e);
        }
    }
}
