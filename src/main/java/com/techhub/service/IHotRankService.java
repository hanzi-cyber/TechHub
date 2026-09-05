package com.techhub.service;

import java.util.List;

/**
 * 帖子热度榜服务:基于 Redis ZSET 维护热度排名
 */
public interface IHotRankService {

    /** 浏览帖子:热度累加浏览权重 */
    void incrView(Long postId);

    /** 点赞:热度累加点赞权重 */
    void incrLike(Long postId);

    /** 取消点赞:热度减去点赞权重 */
    void decrLike(Long postId);

    /** 新增评论:热度累加评论权重 */
    void incrComment(Long postId);

    /** 删除 count 条评论:热度减去评论权重 * count */
    void decrComment(Long postId, int count);

    /** 收藏:热度累加收藏权重 */
    void incrCollect(Long postId);

    /** 取消收藏:热度减去收藏权重 */
    void decrCollect(Long postId);

    /** 热度榜 topN 帖子ID(按热度倒序),区间 [start, end] 闭区间 */
    List<Long> topPostIds(long start, long end);

    /** 热度榜帖子总数 */
    long size();

    /** 全量重建热度榜(启动预热 / Redis 清空后恢复) */
    void rebuild();

    /** 把 ZSET 热度分回写到 t_post.hot_score(定时任务调用,供带筛选的热门排序使用) */
    void syncScoresToDb();

    /** 从热度榜移除帖子(帖子删除时调用,避免已删帖子占用榜单) */
    void removePost(Long postId);
}
