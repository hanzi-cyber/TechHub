package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.entity.Comment;
import com.techhub.entity.User;
import com.techhub.mapper.CommentMapper;
import com.techhub.service.ICommentService;
import com.techhub.service.IUserService;
import com.techhub.vo.CommentVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    /** 评论状态:1正常 */
    private static final int COMMENT_STATUS_NORMAL = 1;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private IUserService userService;

    @Override
    public PageResult<CommentVO> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = commentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .eq(Comment::getStatus, COMMENT_STATUS_NORMAL)
                        .orderByDesc(Comment::getCreatedAt));

        PageResult<CommentVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());

        List<Comment> records = page.getRecords();
        if (records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查评论者(一次 IN 查询,避免 N+1),建立 id -> UserVO 映射
        List<Long> userIds = records.stream().map(Comment::getUserId).distinct().toList();
        Map<Long, UserVO> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }));

        // 逐条组装:每条评论配上它自己的评论者
        List<CommentVO> commentVOS = records.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            vo.setUser(userMap.get(comment.getUserId()));
            return vo;
        }).collect(Collectors.toList());

        result.setRecords(commentVOS);
        return result;
    }
}
