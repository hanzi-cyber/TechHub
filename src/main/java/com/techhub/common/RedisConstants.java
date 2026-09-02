package com.techhub.common;

/**
 * Redis 缓存 key 与参数常量,统一管理避免散落各处
 */
public class RedisConstants {

    private RedisConstants() {
    }

    /** 帖子详情缓存 key 前缀 */
    public static final String POST_DETAIL_KEY_PREFIX = "post:detail:";

    /** 帖子详情缓存互斥锁 key 前缀(防击穿) */
    public static final String POST_DETAIL_LOCK_KEY_PREFIX = "post:detail:lock:";

    /** 帖子热度榜 ZSET key(member=帖子ID, score=加权热度) */
    public static final String POST_HOT_ZSET_KEY = "post:hot";

    /** 帖子详情缓存过期时间(分钟) */
    public static final long POST_DETAIL_TTL_MINUTES = 30;

    /** TTL 随机抖动上限(秒),防止同一批缓存同时过期引发缓存雪崩 */
    public static final int POST_DETAIL_TTL_JITTER_SECONDS = 60;

    /** 缓存空值哨兵:不存在的帖子也缓存,防止缓存穿透 */
    public static final String EMPTY_CACHE_VALUE = "NULL";

    /** 空值缓存过期时间(秒),比正常缓存短,避免真实帖子创建后长时间读不到 */
    public static final long EMPTY_CACHE_TTL_SECONDS = 60;

    /** 互斥锁过期时间(秒),防止持有锁的线程宕机后锁永不释放 */
    public static final long LOCK_TTL_SECONDS = 10;

    /** 抢锁失败后的重试间隔(毫秒) */
    public static final long RETRY_SLEEP_MS = 50;

    /** 抢锁最大重试次数,耗尽后直接回源兜底(不写缓存)保证可用 */
    public static final int MAX_RETRY = 3;
}
