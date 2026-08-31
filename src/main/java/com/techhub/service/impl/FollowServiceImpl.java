package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.entity.Follow;
import com.techhub.entity.User;
import com.techhub.mapper.FollowMapper;
import com.techhub.mapper.UserMapper;
import com.techhub.service.IFollowService;
import com.techhub.vo.FollowResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowResultVO follow(Long followeeId) {
        Long userId = BaseContext.getCurrentId();
        validateTarget(userId, followeeId);
        // 原子 upsert:唯一键保证不重复插入,ON DUPLICATE KEY 保证并发安全、幂等
        followMapper.follow(userId, followeeId);
        return new FollowResultVO(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowResultVO unfollow(Long followeeId) {
        Long userId = BaseContext.getCurrentId();
        validateTarget(userId, followeeId);
        // 仅当已关注时才置 0,幂等
        followMapper.unfollow(userId, followeeId);
        return new FollowResultVO(false);
    }

    /** 校验被关注用户合法(非空、存在、不能关注自己) */
    private void validateTarget(Long userId, Long followeeId) {
        if (followeeId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userId.equals(followeeId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = userMapper.selectById(followeeId);
        if (target == null) {
            throw new BusinessException("该用户不存在");
        }
    }
}
