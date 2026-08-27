package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.entity.User;
import com.techhub.service.IUserService;
import com.techhub.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/me")
    public Result<UserVO> getUser() {
        UserVO userVO = userService.getUser();
        return Result.success(userVO);
    }
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }
    @PutMapping("/me")
    public Result<UserVO> updateUser(@RequestBody UpdateUserDTO updateUserDTO) {
        UserVO userVO = userService.updateUser(updateUserDTO);
        return Result.success(userVO);
    }

}
