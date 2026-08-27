package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.service.IUserService;
import com.techhub.vo.PostVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/me")
    public Result<UserVO> getUser() {
        UserVO userVO = userService.getUser();
        return Result.success(userVO);
    }

    /**
     * 根据用户ID获取用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     * @param updateUserDTO
     * @return
     */
    @PutMapping("/me")
    public Result<UserVO> updateUser(@RequestBody UpdateUserDTO updateUserDTO) {
        UserVO userVO = userService.updateUser(updateUserDTO);
        return Result.success(userVO);
    }

    /**
     * 分页获取用户发布的帖子
     * @param id 用户ID
     * @param pageNum 页码,默认1
     * @param pageSize 每页条数,默认10
     * @return
     */
    @GetMapping("/{id}/posts")
    public Result<PageResult<PostVO>> getUserPosts(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PostVO> pageResult = userService.getUserPosts(id, pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 分页获取用户的粉丝列表
     * @param id 用户ID
     * @param pageNum 页码,默认1
     * @param pageSize 每页条数,默认10
     * @return
     */
    @GetMapping("/{id}/followers")
    public Result<PageResult<UserVO>> getFollowers(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserVO> pageResult = userService.getFollowers(id, pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 分页获取用户的关注列表
     * @param id 用户ID
     * @param pageNum 页码,默认1
     * @param pageSize 每页条数,默认10
     * @return
     */
    @GetMapping("/{id}/following")
    public Result<PageResult<UserVO>> getFollowing(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserVO> pageResult = userService.getFollowing(id, pageNum, pageSize);
        return Result.success(pageResult);
    }

}
