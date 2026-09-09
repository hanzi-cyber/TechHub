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

    /** 帖子浏览量计数 hash key(field=帖子ID, value=自上次落库以来的浏览增量) */
    public static final String POST_VIEW_COUNT_KEY = "post:view:count";

    /** 限流计数 key 前缀(固定窗口) */
    public static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:";

    /** 幂等 key 前缀 */
    public static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";

    /** 登录态 token 缓存 key 前缀(分布式会话 / 单点登录:同一 userId 只保留一个活跃 token) */
    public static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";

    /** 帖子详情缓存过期时间(分钟) */
    public static final long POST_DETAIL_TTL_MINUTES = 30;

    /** TTL 随机抖动上限(秒),防止同一批缓存同时过期引发缓存雪崩 */
    public static final int POST_DETAIL_TTL_JITTER_SECONDS = 60;

    /** 缓存空值哨兵:不存在的帖子也缓存,防止缓存穿透 */
    public static final String EMPTY_CACHE_VALUE = "NULL";

    /** 空值缓存过期时间(秒),比正常缓存短,避免真实帖子创建后长时间读不到 */
    public static final long EMPTY_CACHE_TTL_SECONDS = 60;

    /** 抢锁的最长等待时间(秒):等待期间持锁线程重建缓存,拿到锁后靠「双检」命中缓存;超时则回源兜底 */
    public static final long LOCK_WAIT_SECONDS = 2;

    /** 延迟双删的延迟时间(毫秒):先更库后删缓存,提交后隔这段时间再删一次,兜底并发读回源重建旧缓存的窗口 */
    public static final long CACHE_DELAYED_EVICT_MS = 500;
}
