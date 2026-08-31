package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.entity.Follow;
import com.techhub.vo.FollowResultVO;

public interface IFollowService extends IService<Follow> {

    /**
     * 关注(幂等),返回操作后的关注状态
     *
     * @param followeeId 被关注用户ID
     */
    FollowResultVO follow(Long followeeId);

    /**
     * 取消关注(幂等),返回操作后的关注状态
     *
     * @param followeeId 被关注用户ID
     */
    FollowResultVO unfollow(Long followeeId);
}
