package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.entity.User;
import com.techhub.vo.PostVO;
import com.techhub.vo.UserVO;

public interface IUserService extends IService<User> {
    UserVO getUser();

    UserVO getUserById(Long id);

    UserVO updateUser(UpdateUserDTO updateUserDTO);

    /**
     * 分页查询用户发布的帖子
     * @param userId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页条数
     * @return 分页结果(含作者、标签)
     */
    PageResult<PostVO> getUserPosts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 分页查询用户的粉丝列表
     * @param userId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<UserVO> getFollowers(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 分页查询用户的关注列表
     * @param userId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<UserVO> getFollowing(Long userId, Integer pageNum, Integer pageSize);
}
