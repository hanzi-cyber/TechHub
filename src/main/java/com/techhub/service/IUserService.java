package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.entity.User;
import com.techhub.vo.UserVO;

public interface IUserService extends IService<User> {
    UserVO getUser();

    UserVO getUserById(Long id);

    UserVO updateUser(UpdateUserDTO updateUserDTO);
}
