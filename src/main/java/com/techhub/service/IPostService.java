package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.dto.SavePostDTO;
import com.techhub.entity.Post;
import com.techhub.enumsort.SortType;
import com.techhub.vo.PostVO;

public interface IPostService extends IService<Post> {
    PageResult<PostVO> getPosts(Integer pageNum, Integer pageSize, SortType sort, String keyword, Integer tagId);

    /**
     * 关注流(拉模式):分页查询当前用户关注的人发布的帖子
     *
     * @param pageNum  页码,从1开始
     * @param pageSize 每页条数
     */
    PageResult<PostVO> getFollowFeed(Integer pageNum, Integer pageSize);

    PostVO getPostById(Long id);

    PostVO createPost(SavePostDTO savePostDTO);

    /**
     * 更新帖子(仅作者本人)
     *
     * @param id          帖子ID
     * @param savePostDTO 新标题/正文/摘要/标签
     */
    PostVO updatePost(Long id, SavePostDTO savePostDTO);

    /**
     * 删除帖子(仅作者本人,软删)
     *
     * @param id 帖子ID
     */
    void deletePost(Long id);

    /**
     * 浏览 +1:仅累加到 Redis 计数 hash(field=postId),不写 DB。
     * 由定时任务批量回写,避免每次浏览都写 MySQL 造成写放大热点。
     *
     * @param postId 帖子ID
     */
    void incrViewCount(Long postId);

    /**
     * 把 Redis 中的浏览量增量批量回写到 t_post.view_count(定时任务调用)
     */
    void syncViewCountsToDb();

    /**
     * 失效帖子详情缓存(帖子数据变更时调用,如点赞/收藏/评论数变化)
     *
     * @param postId 帖子ID
     */
    void evictPostCache(Long postId);
}
