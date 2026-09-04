package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.techhub.common.RedisConstants;
import com.techhub.entity.Post;
import com.techhub.mapper.PostMapper;
import com.techhub.service.IHotRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 热度榜实现:ZSET 的 member 存帖子ID,score 存加权热度。
 * 权重体现不同行为的价值差异:收藏 > 点赞 > 评论 > 浏览。
 */
@Slf4j
@Service
public class HotRankServiceImpl implements IHotRankService {

    /** 热度权重(面试点:权重可调,用于控制榜单偏向) */
    private static final double VIEW_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 2.0;
    private static final double COLLECT_WEIGHT = 5.0;

    /** 帖子状态:1已发布 */
    private static final int POST_STATUS_PUBLISHED = 1;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PostMapper postMapper;

    @Override
    public void incrView(Long postId) {
        changeScore(postId, VIEW_WEIGHT);
    }

    @Override
    public void incrLike(Long postId) {
        changeScore(postId, LIKE_WEIGHT);
    }

    @Override
    public void decrLike(Long postId) {
        changeScore(postId, -LIKE_WEIGHT);
    }

    @Override
    public void incrComment(Long postId) {
        changeScore(postId, COMMENT_WEIGHT);
    }

    @Override
    public void decrComment(Long postId, int count) {
        changeScore(postId, -COMMENT_WEIGHT * count);
    }

    @Override
    public void incrCollect(Long postId) {
        changeScore(postId, COLLECT_WEIGHT);
    }

    @Override
    public void decrCollect(Long postId) {
        changeScore(postId, -COLLECT_WEIGHT);
    }

    @Override
    public List<Long> topPostIds(long start, long end) {
        Set<String> ids = stringRedisTemplate.opsForZSet()
                .reverseRange(RedisConstants.POST_HOT_ZSET_KEY, start, end);
        List<Long> result = new ArrayList<>();
        if (ids != null) {
            for (String id : ids) {
                result.add(Long.valueOf(id));
            }
        }
        return result;
    }

    @Override
    public long size() {
        Long size = stringRedisTemplate.opsForZSet().zCard(RedisConstants.POST_HOT_ZSET_KEY);
        return size == null ? 0 : size;
    }

    @Override
    public void rebuild() {
        stringRedisTemplate.delete(RedisConstants.POST_HOT_ZSET_KEY);
        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>().eq(Post::getStatus, POST_STATUS_PUBLISHED));
        for (Post post : posts) {
            double score = nvl(post.getViewCount()) * VIEW_WEIGHT
                    + nvl(post.getLikeCount()) * LIKE_WEIGHT
                    + nvl(post.getCommentCount()) * COMMENT_WEIGHT
                    + nvl(post.getCollectCount()) * COLLECT_WEIGHT;
            // 只把有热度的帖子放进榜,避免大量零分成员拖慢查询
            if (score > 0) {
                stringRedisTemplate.opsForZSet().add(RedisConstants.POST_HOT_ZSET_KEY,
                        String.valueOf(post.getId()), score);
            }
        }
        log.info("热度榜重建完成,共 {} 条帖子", posts.size());
    }

    @Override
    public void syncScoresToDb() {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(RedisConstants.POST_HOT_ZSET_KEY, 0, -1);
        if (tuples == null || tuples.isEmpty()) {
            log.info("热度榜为空,跳过 hot_score 回写");
            return;
        }
        // 逐条回写 hot_score。帖子量很大时可改为单条 CASE WHEN 批量更新,这里为清晰用逐条。
        int count = 0;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long postId = Long.valueOf(tuple.getValue());
            long score = tuple.getScore() == null ? 0 : tuple.getScore().longValue();
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, postId)
                    .set(Post::getHotScore, score));
            count++;
        }
        log.info("hot_score 回写完成,共更新 {} 条帖子", count);
    }

    /** 帖子热度增减(统一入口) */
    private void changeScore(Long postId, double delta) {
        if (postId == null) {
            return;
        }
        stringRedisTemplate.opsForZSet().incrementScore(RedisConstants.POST_HOT_ZSET_KEY,
                String.valueOf(postId), delta);
    }

    /** Integer 转 int,空值按 0 处理(计数字段理论上非空,防御性写法) */
    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
