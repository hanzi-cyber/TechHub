package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.entity.Post;
import com.techhub.entity.PostTag;
import com.techhub.entity.Tag;
import com.techhub.entity.User;
import com.techhub.enumsort.SortType;
import com.techhub.mapper.PostMapper;
import com.techhub.mapper.PostTagMapper;
import com.techhub.mapper.TagMapper;
import com.techhub.service.IPostService;
import com.techhub.service.IUserService;
import com.techhub.vo.PostVO;
import com.techhub.vo.TagVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private static final int POST_STATUS_PUBLISHED = 1;

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private PostTagMapper postTagMapper;
    @Autowired
    private TagMapper tagMapper;

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

        // 排序:热门按热度分倒序;最新纯粹按发布时间倒序(置顶是个人主页的概念,首页不叠加)
        if (sort == SortType.HOT) {
            wrapper.orderByDesc(Post::getHotScore).orderByDesc(Post::getPublishedAt);
        } else {
            wrapper.orderByDesc(Post::getPublishedAt);
        }

        Page<Post> page = postMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Post> posts = page.getRecords();

        PageResult<PostVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());

        if (posts.isEmpty()) {
            pageResult.setRecords(Collections.emptyList());
            return pageResult;
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
        List<PostVO> postVOs = posts.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setAuthor(authorMap.get(post.getUserId()));
            postVO.setTags(postTagsVOMap.getOrDefault(post.getId(), Collections.emptyList()));
            return postVO;
        }).collect(Collectors.toList());

        pageResult.setRecords(postVOs);
        return pageResult;
    }

    /**
     * 根据ID获取帖子
     *
     * @param id 帖子ID
     * @return 帖子VO
     */

    @Override
    public PostVO getPostById(Long id) {
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

    /** 返回空的分页结果 */
    private PageResult<PostVO> emptyPage(Integer pageNum, Integer pageSize) {
        PageResult<PostVO> empty = new PageResult<>();
        empty.setTotal(0);
        empty.setPageNum(pageNum);
        empty.setPageSize(pageSize);
        empty.setRecords(Collections.emptyList());
        return empty;
    }
}
