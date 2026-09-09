-- =============================================================
-- 清理 JMeter 压测产生的脏数据
-- 用法:在 techhub 库上执行(DataGrip / Navicat 直接跑,或 mysql -uroot -p techhub < cleanup_test_data.sql)
-- 注意:Redis 部分不是 SQL,见文件末尾说明,需用 redis-cli 单独执行
-- =============================================================

USE `techhub`;

-- ---------------------------------------------------------------
-- 先删关联记录(子查询依赖 t_user 里的压测账号,所以必须先于删用户)
-- ---------------------------------------------------------------

-- 1. 通知:压测用户发出的 + 发给压测用户的
--    若本来就没有真实通知,直接 TRUNCATE t_notification; 更省事
DELETE FROM t_notification
WHERE sender_id IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%')
   OR user_id   IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%');

-- 2. 评论
DELETE FROM t_comment
WHERE user_id IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%');

-- 3. 点赞
DELETE FROM t_like_record
WHERE user_id IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%');

-- 4. 收藏
DELETE FROM t_collect_record
WHERE user_id IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%');

-- 5. 关注:压测用户发起的 + 指向压测用户的
DELETE FROM t_follow
WHERE user_id    IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%')
   OR followee_id IN (SELECT id FROM t_user WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%');

-- ---------------------------------------------------------------
-- 再删压测账号本身
-- ---------------------------------------------------------------
DELETE FROM t_user
WHERE username LIKE 'jmeter_read_%' OR username LIKE 'jmeter_write_%';

-- ---------------------------------------------------------------
-- 重建帖子冗余计数:按剩余真实记录重新统计,修正被压测抬高的 like/comment/collect 数
-- ---------------------------------------------------------------
UPDATE t_post
SET like_count    = (SELECT COUNT(*) FROM t_like_record   l WHERE l.target_type = 1 AND l.target_id = t_post.id AND l.status = 1),
    comment_count = (SELECT COUNT(*) FROM t_comment       c WHERE c.post_id = t_post.id AND c.status = 1),
    collect_count = (SELECT COUNT(*) FROM t_collect_record r WHERE r.post_id = t_post.id AND r.status = 1);

-- =============================================================
-- Redis 清理(非 SQL,用 redis-cli 执行,Redis 在 192.168.100.128:6379)
--
-- 压测污染了:热度榜 ZSET(post:hot)、浏览量 hash(post:view:count)、详情缓存(post:detail:*)
--
-- 开发环境最省事(整个 DB0 清空,会踢掉所有登录态,重新登录即可):
--   redis-cli -h 192.168.100.128 FLUSHDB
--
-- 或只清相关 key:
--   redis-cli -h 192.168.100.128 DEL post:hot post:view:count
--   redis-cli -h 192.168.100.128 --scan --pattern 'post:detail:*' | xargs -r redis-cli -h 192.168.100.128 DEL
--
-- rate:limit:* / idempotent:* / login:token:* 都有 TTL,会自动过期,无需手动清
-- =============================================================
