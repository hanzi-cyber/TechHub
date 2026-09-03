package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techhub.common.PageResult;
import com.techhub.common.RedisConstants;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.SavePostDTO;
import com.techhub.entity.CollectRecord;
import com.techhub.entity.LikeRecord;
import com.techhub.entity.Post;
import com.techhub.entity.PostTag;
import com.techhub.entity.Tag;
import com.techhub.entity.User;
import com.techhub.enumsort.SortType;
import com.techhub.mapper.CollectRecordMapper;
import com.techhub.mapper.LikeRecordMapper;
import com.techhub.mapper.PostMapper;
import com.techhub.mapper.PostTagMapper;
import com.techhub.mapper.TagMapper;
import com.techhub.service.IHotRankService;
import com.techhub.service.IPostService;
import com.techhub.service.ITagService;
import com.techhub.service.IUserService;
import com.techhub.vo.PostVO;
import com.techhub.vo.TagVO;
import com.techhub.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private static final int POST_STATUS_PUBLISHED = 1;

    /** 点赞目标类型:1帖子 */
    private static final int LIKE_TARGET_POST = 1;

    /** 点赞/收藏记录有效状态:1有效 */
    private static final int RECORD_ACTIVE = 1;

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private PostTagMapper postTagMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ITagService tagService;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private CollectRecordMapper collectRecordMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IHotRankService hotRankService;

    /**
     * 分页查询帖子列表(首页/搜索)
     *
     * @param pageNum  页码,从1开始
     * @param pageSize 每页条数
     * @param sort     排序方式
     * @param keyword  关键词(标题/摘要)
     * @param tagId    标签ID过滤
     */
    @Override
    public PageResult<PostVO> getPosts(Integer pageNum, Integer pageSize, SortType sort, String keyword, Integer tagId) {
        // 首页纯热门列表(无关键词/标签筛选)走 Redis 热度榜
        boolean plainHot = sort == SortType.HOT
                && (keyword == null || keyword.trim().isEmpty()) && tagId == null;
        if (plainHot) {
            return getHotPostsByZSet(pageNum, pageSize);
        }

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, POST_STATUS_PUBLISHED);

        // 关键词:同时模糊匹配标题和摘要
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getSummary, keyword));
        }

        // 标签过滤:先查出该标签下的帖子ID
        if (tagId != null) {
            List<Long> postIds = postTagMapper.selectList(
                            new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tagId.longValue()))
                    .stream().map(PostTag::getPostId).collect(Collectors.toList());
            if (postIds.isEmpty()) {
                return emptyPage(pageNum, pageSize);
            }
            wrapper.in(Post::getId, postIds);
        }

        // 排序:热门(带筛选时)按热度分倒序;最新纯粹按发布时间倒序
        if (sort == SortType.HOT) {
            wrapper.orderByDesc(Post::getHotScore).orderByDesc(Post::getPublishedAt);
        } else {
            wrapper.orderByDesc(Post::getPublishedAt);
        }

        Page<Post> page = postMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        PageResult<PostVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setRecords(assemblePostVOs(page.getRecords()));
        return pageResult;
    }

    /** 首页热门列表:从 Redis 热度榜(ZSET)取 topN 帖子ID,再回源查库组装 */
    private PageResult<PostVO> getHotPostsByZSet(Integer pageNum, Integer pageSize) {
        long start = (long) (pageNum - 1) * pageSize;
        long end = start + pageSize - 1;
        List<Long> ids = hotRankService.topPostIds(start, end);

        PageResult<PostVO> result = new PageResult<>();
        result.setTotal(hotRankService.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        if (ids.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 回源查 DB,按热度榜顺序组装,并过滤已删除/非发布的帖子
        Map<Long, Post> postMap = this.listByIds(ids).stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == POST_STATUS_PUBLISHED)
                .collect(Collectors.toMap(Post::getId, p -> p));
        List<Post> ordered = ids.stream()
                .map(postMap::get)
                .filter(p -> p != null)
                .collect(Collectors.toList());
        result.setRecords(assemblePostVOs(ordered));
        return result;
    }

    /**
     * 关注流(拉模式):实时查询我关注的人发布的帖子,按发布时间倒序。
     * 拉模式特点:写简单(发帖不扇出)、天然一致、无脏数据;规模大后可演进为推拉结合。
     */
    @Override
    public PageResult<PostVO> getFollowFeed(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return emptyPage(pageNum, pageSize);
        }
        IPage<Post> page = postMapper.selectFeedPage(new Page<>(pageNum, pageSize), userId);
        PageResult<PostVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setRecords(assemblePostVOs(page.getRecords()));
        return result;
    }

    /** 批量组装帖子 VO(作者 + 标签),供 MySQL 分页与热度榜两条路径复用 */
    private List<PostVO> assemblePostVOs(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查作者(避免 N+1)
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        Map<Long, UserVO> authorMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }));

        // 批量查标签关联
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().in(PostTag::getPostId, postIds));

        Map<Long, List<TagVO>> postTagsVOMap;
        if (!postTags.isEmpty()) {
            Set<Long> tagIds = postTags.stream().map(PostTag::getTagId).collect(Collectors.toSet());
            Map<Long, TagVO> tagVOMap = tagMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(Tag::getId, tag -> {
                        TagVO tagVO = new TagVO();
                        BeanUtils.copyProperties(tag, tagVO);
                        return tagVO;
                    }));
            postTagsVOMap = postTags.stream()
                    .filter(pt -> tagVOMap.containsKey(pt.getTagId()))
                    .collect(Collectors.groupingBy(PostTag::getPostId,
                            Collectors.mapping(pt -> tagVOMap.get(pt.getTagId()), Collectors.toList())));
        } else {
            postTagsVOMap = new HashMap<>();
        }

        // 组装 VO
        return posts.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setAuthor(authorMap.get(post.getUserId()));
            postVO.setTags(postTagsVOMap.getOrDefault(post.getId(), Collections.emptyList()));
            return postVO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据ID获取帖子(带缓存:cache-aside 读模式 + 防击穿/穿透/雪崩)
     *
     * @param id 帖子ID
     * @return 帖子VO
     */
    @Override
    public PostVO getPostById(Long id) {
        PostVO vo = getPostFromCacheOrDb(id);
        if (vo != null) {
            // 浏览 +1:累加 DB 浏览量并更新热度榜;不失效详情缓存,浏览数允许短暂延迟
            recordView(id);
        }
        return vo;
    }

    /** 从缓存或数据库取帖子详情(含用户相关字段补齐) */
    private PostVO getPostFromCacheOrDb(Long id) {
        String key = RedisConstants.POST_DETAIL_KEY_PREFIX + id;
        // 1、先查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            // 命中空值哨兵:帖子确认不存在,防穿透,直接返回
            if (RedisConstants.EMPTY_CACHE_VALUE.equals(json)) {
                return null;
            }
            PostVO vo = deserialize(json);
            if (vo != null) {
                // 用户相关字段不入缓存,命中的帖子在这里现查补齐
                fillUserSpecific(vo, id);
                return vo;
            }
            // 反序列化失败(脏缓存)则落到下面回源重建
        }
        // 2、缓存未命中:互斥锁防击穿 + 回源重建
        PostVO vo = queryWithMutexLock(id);
        if (vo != null) {
            fillUserSpecific(vo, id);
        }
        return vo;
    }

    /** 记录一次浏览:DB 浏览量原子 +1,热度榜 score 累加浏览权重 */
    private void recordView(Long postId) {
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("view_count = view_count + 1"));
        hotRankService.incrView(postId);
    }

    /** 当前用户是否已点赞该帖子 */
    private boolean isLikedByCurrentUser(Long postId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return false;
        }
        return likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, LIKE_TARGET_POST)
                .eq(LikeRecord::getTargetId, postId)
                .eq(LikeRecord::getStatus, RECORD_ACTIVE)) > 0;
    }

    /** 当前用户是否已收藏该帖子 */
    private boolean isCollectedByCurrentUser(Long postId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return false;
        }
        return collectRecordMapper.selectCount(new LambdaQueryWrapper<CollectRecord>()
                .eq(CollectRecord::getUserId, userId)
                .eq(CollectRecord::getPostId, postId)
                .eq(CollectRecord::getStatus, RECORD_ACTIVE)) > 0;
    }

    /**
     * 创建帖子
     *
     * @param savePostDTO 帖子DTO
     * @return 帖子VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(SavePostDTO savePostDTO) {
        Long userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();

        // 1、保存帖子主记录(save 后 MP 会把自增 id 回填到 post.id)
        Post post = new Post();
        BeanUtils.copyProperties(savePostDTO, post);
        post.setUserId(userId);
        post.setStatus(POST_STATUS_PUBLISHED);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setHotScore(0L);
        post.setIsTop(0);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        post.setPublishedAt(now);
        this.save(post);

        // 2、保存帖子-标签关联
        List<TagVO> tagVOs = new ArrayList<>();
        if (savePostDTO.getTagIds() != null && !savePostDTO.getTagIds().isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(savePostDTO.getTagIds());
            for (Tag tag : tags) {
                PostTag postTag = new PostTag();
                postTag.setPostId(post.getId());
                postTag.setTagId(tag.getId());
                postTagMapper.insert(postTag);

                TagVO tagVO = new TagVO();
                BeanUtils.copyProperties(tag, tagVO);
                tagVOs.add(tagVO);
            }
        }

        // 3、组装返回
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        UserVO author = userService.getUserById(userId);
        postVO.setAuthor(author);
        postVO.setTags(tagVOs);
        return postVO;
    }

    /** 返回空的分页结果 */
    private PageResult<PostVO> emptyPage(Integer pageNum, Integer pageSize) {
        PageResult<PostVO> empty = new PageResult<>();
        empty.setTotal(0);
        empty.setPageNum(pageNum);
        empty.setPageSize(pageSize);
        empty.setRecords(Collections.emptyList());
        return empty;
    }

    // ==================== 帖子详情缓存(cache-aside) ====================

    @Override
    public void evictPostCache(Long postId) {
        if (postId == null) {
            return;
        }
        stringRedisTemplate.delete(RedisConstants.POST_DETAIL_KEY_PREFIX + postId);
    }

    /** 回源查库并组装帖子 VO(不含 liked/collected 等用户相关字段) */
    private PostVO buildPostVO(Long id) {
        Post post = getById(id);
        if (post == null || post.getStatus() != POST_STATUS_PUBLISHED) {
            return null;
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setAuthor(userService.getUserById(post.getUserId()));
        postVO.setTags(postTagMapper.getTagsByPostId(id));
        return postVO;
    }

    /**
     * 互斥锁防击穿:抢锁 → 双检 → 回源 → 写缓存。
     * 抢不到锁就短暂等待重试;重试耗尽直接回源兜底(不写缓存),保证可用性。
     */
    private PostVO queryWithMutexLock(Long id) {
        String key = RedisConstants.POST_DETAIL_KEY_PREFIX + id;
        String lockKey = RedisConstants.POST_DETAIL_LOCK_KEY_PREFIX + id;
        for (int i = 0; i < RedisConstants.MAX_RETRY; i++) {
            Boolean locked = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofSeconds(RedisConstants.LOCK_TTL_SECONDS));
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双检:拿到锁的线程可能发现缓存已被别的线程重建
                    String json = stringRedisTemplate.opsForValue().get(key);
                    if (json != null) {
                        return RedisConstants.EMPTY_CACHE_VALUE.equals(json) ? null : deserialize(json);
                    }
                    PostVO vo = buildPostVO(id);
                    cachePost(id, vo);
                    return vo;
                } finally {
                    stringRedisTemplate.delete(lockKey);
                }
            }
            // 没抢到锁,短暂等待后重试
            try {
                Thread.sleep(RedisConstants.RETRY_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // 重试耗尽:直接回源兜底,不写缓存
        return buildPostVO(id);
    }

    /** 写缓存:帖子不存在时缓存空值哨兵(防穿透);存在时带随机 TTL(防雪崩) */
    private void cachePost(Long id, PostVO vo) {
        String key = RedisConstants.POST_DETAIL_KEY_PREFIX + id;
        if (vo == null) {
            stringRedisTemplate.opsForValue().set(key, RedisConstants.EMPTY_CACHE_VALUE,
                    Duration.ofSeconds(RedisConstants.EMPTY_CACHE_TTL_SECONDS));
            return;
        }
        // 用户相关字段不入缓存,避免 A 用户的状态污染 B 用户
        clearUserSpecific(vo);
        //随机TTL防止缓存雪崩
        Duration ttl = Duration.ofMinutes(RedisConstants.POST_DETAIL_TTL_MINUTES)
                .plusSeconds(ThreadLocalRandom.current().nextInt(RedisConstants.POST_DETAIL_TTL_JITTER_SECONDS + 1));
        stringRedisTemplate.opsForValue().set(key, serialize(vo), ttl);
    }

    /** 缓存命中的帖子补齐用户相关字段(点赞/收藏状态,现查,不入缓存) */
    private void fillUserSpecific(PostVO vo, Long postId) {
        vo.setLiked(isLikedByCurrentUser(postId));
        vo.setCollected(isCollectedByCurrentUser(postId));
    }

    /** 写缓存前清空用户相关字段 */
    private void clearUserSpecific(PostVO vo) {
        vo.setLiked(null);
        vo.setCollected(null);
        if (vo.getAuthor() != null) {
            vo.getAuthor().setFollowed(null);
        }
    }

    private String serialize(PostVO vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (Exception e) {
            throw new BusinessException("帖子缓存序列化失败");
        }
    }

    private PostVO deserialize(String json) {
        try {
            return objectMapper.readValue(json, PostVO.class);
        } catch (Exception e) {
            log.warn("帖子缓存反序列化失败,回源重建: {}", e.getMessage());
            return null;
        }
    }
}
