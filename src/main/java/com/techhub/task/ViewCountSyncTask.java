package com.techhub.task;

import com.techhub.service.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务:把 Redis 中的浏览量增量回写到 t_post.view_count。
 * 浏览是高频操作,若每次浏览都直接写 MySQL,会成为写放大热点;
 * 这里改为 Redis 原子计数 + 定时批量落库,兼顾准确性与性能。
 */
@Slf4j
@Component
public class ViewCountSyncTask {

    @Autowired
    private IPostService postService;

    /** 每 5 分钟回写一次(比热度榜任务错开 30 秒,避免同时打到 DB) */
    @Scheduled(cron = "30 */5 * * * ?")
    public void syncViewCount() {
        try {
            postService.syncViewCountsToDb();
        } catch (Exception e) {
            // 回写失败不阻断后续调度,下个周期重试
            log.error("浏览量异步落库失败", e);
        }
    }
}
