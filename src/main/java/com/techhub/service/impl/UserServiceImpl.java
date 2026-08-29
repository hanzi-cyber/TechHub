package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.entity.Follow;
import com.techhub.entity.Post;
import com.techhub.entity.PostTag;
import com.techhub.entity.Tag;
import com.techhub.entity.User;
import com.techhub.mapper.FollowMapper;
import com.techhub.mapper.PostMapper;
import com.techhub.mapper.PostTagMapper;
import com.techhub.mapper.TagMapper;
import com.techhub.mapper.UserMapper;
import com.techhub.service.IUserService;
import com.techhub.vo.PostVO;
import com.techhub.vo.TagVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /** 帖子状态:1已发布 */
    private static final int POST_STATUS_PUBLISHED = 1;

    /** 关注状态:1已关注 */
    private static final int FOLLOW_STATUS_ACTIVE = 1;

    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final FollowMapper followMapper;

    public UserServiceImpl(PostMapper postMapper, TagMapper tagMapper, PostTagMapper postTagMapper, FollowMapper followMapper) {
        this.postMapper = postMapper;
        this.tagMapper = tagMapper;
        this.postTagMapper = postTagMapper;
        this.followMapper = followMapper;
    }

    @Override
    public UserVO getUser() {
        Long userId = BaseContext.getCurrentId();
        User user = getById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


    @Override
    public UserVO updateUser(UpdateUserDTO updateUserDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 用户名:非空且发生变化时,校验唯一性(登录名,不能重复)
        String username = updateUserDTO.getUsername();
        if (username != null && !username.isEmpty() && !username.equals(user.getUsername())) {
            checkUsernameUnique(username);
            user.setUsername(username);
        }

        // 邮箱:非空且发生变化时,校验唯一性
        String email = updateUserDTO.getEmail();
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            checkEmailUnique(email);
            user.setEmail(email);
        }

        // 手机号:非空且发生变化时,校验唯一性
        String phone = updateUserDTO.getPhone();
        if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone())) {
            checkPhoneUnique(phone);
            user.setPhone(phone);
        }

        if(updateUserDTO.getBio() != null && !updateUserDTO.getBio().isEmpty())
            user.setBio(updateUserDTO.getBio());
        if(updateUserDTO.getAvatarUrl() != null && !updateUserDTO.getAvatarUrl().isEmpty())
            user.setAvatarUrl(updateUserDTO.getAvatarUrl());
        updateById(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /** 校验用户名是否已被占用 */
    private void checkUsernameUnique(String username) {
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new BusinessException("用户名已被占用");
        }
    }

    /** 校验邮箱是否已被使用 */
    private void checkEmailUnique(String email) {
        if (count(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new BusinessException("邮箱已被使用");
        }
    }

    /** 校验手机号是否已被使用 */
    private void checkPhoneUnique(String phone) {
        if (count(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0) {
            throw new BusinessException("手机号已被使用");
        }
    }


    @Override
    public PageResult<PostVO> getUserPosts(Long userId, Integer pageNum, Integer pageSize) {
        // 1、分页查询该用户已发布的帖子,置顶优先、再按创建时间倒序
        Page<Post> page = postMapper.selectPage(

                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .eq(Post::getStatus, POST_STATUS_PUBLISHED)
                        .orderByDesc(Post::getIsTop)
                        .orderByDesc(Post::getCreatedAt));

        List<Post> posts = page.getRecords();
        if (posts.isEmpty()) {
            PageResult<PostVO> emptyResult = new PageResult<>();
            emptyResult.setRecords(Collections.emptyList());
            emptyResult.setTotal(page.getTotal());
            emptyResult.setPageNum(page.getCurrent());
            emptyResult.setPageSize(page.getSize());
            return emptyResult;
        }

        // 2、查询作者信息(整个列表都是同一个作者,查一次即可)
        User user = getById(userId);
        UserVO author = new UserVO();
        if (user != null) {
            BeanUtils.copyProperties(user, author);
        }

        // 3、批量查询这些帖子的标签关联
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().in(PostTag::getPostId, postIds));

        // 4、批量查询标签详情,按 postId 分组
        Map<Long, List<TagVO>> postTagVOMap = new HashMap<>();
        if (!postTags.isEmpty()) {
            Set<Long> tagIds = postTags.stream().map(PostTag::getTagId).collect(Collectors.toSet());
            Map<Long, TagVO> tagVOMap = tagMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(Tag::getId, tag -> {
                        TagVO tagVO = new TagVO();
                        BeanUtils.copyProperties(tag, tagVO);
                        return tagVO;
                    }));
            Map<Long, List<TagVO>> grouped = postTags.stream()
                    .filter(pt -> tagVOMap.containsKey(pt.getTagId()))
                    .collect(Collectors.groupingBy(PostTag::getPostId,
                            Collectors.mapping(pt -> tagVOMap.get(pt.getTagId()), Collectors.toList())));
            postTagVOMap.putAll(grouped);
        }

        // 5、组装 VO
        List<PostVO> postVOs = posts.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setAuthor(author);
            postVO.setTags(postTagVOMap.getOrDefault(post.getId(), Collections.emptyList()));
            return postVO;
        }).collect(Collectors.toList());

        // 6、转成分页返回体
        PageResult<PostVO> result = new PageResult<>();
        result.setRecords(postVOs);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    /**
     * 获取用户粉丝列表
     * @param userId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public PageResult<UserVO> getFollowers(Long userId, Integer pageNum, Integer pageSize) {
        // 粉丝 = 被关注者(followeeId)等于该用户的关注关系
        Page<Follow> page = followMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFolloweeId, userId)
                        .eq(Follow::getStatus, FOLLOW_STATUS_ACTIVE)
                        .orderByDesc(Follow::getCreatedAt));
        // 记录里的 userId 即粉丝 id
        return buildUserPageResult(page, Follow::getUserId);
    }

    /**
     * 获取用户关注列表
     * @param userId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public PageResult<UserVO> getFollowing(Long userId, Integer pageNum, Integer pageSize) {
        // 关注 = 关注者(userId)等于该用户的关注关系
        Page<Follow> page = followMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId, userId)
                        .eq(Follow::getStatus, FOLLOW_STATUS_ACTIVE)
                        .orderByDesc(Follow::getCreatedAt));
        // 记录里的 followeeId 即被关注者 id
        return buildUserPageResult(page, Follow::getFolloweeId);
    }

    /**
     * 把一页关注关系转成分页的用户列表
     *
     * @param page              关注关系分页
     * @param targetIdExtractor 从 Follow 里取出目标用户 id(粉丝取 userId,关注取 followeeId)
     */
    private PageResult<UserVO> buildUserPageResult(Page<Follow> page, Function<Follow, Long> targetIdExtractor) {
        PageResult<UserVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());

        List<Follow> follows = page.getRecords();
        if (follows.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 目标用户 id(去重)
        List<Long> targetIds = follows.stream()
                .map(targetIdExtractor)
                .distinct()
                .collect(Collectors.toList());

        // 批量查用户,建立 id -> UserVO 映射
        Map<Long, UserVO> userVOMap = this.listByIds(targetIds).stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }));

        // 按原顺序映射回 UserVO,查不到的(已注销)跳过
        List<UserVO> records = follows.stream()
                .map(targetIdExtractor)
                .map(userVOMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        result.setRecords(records);
        return result;
    }
}
