package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.UpdateUserDTO;
import com.techhub.entity.User;
import com.techhub.mapper.UserMapper;
import com.techhub.service.IUserService;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

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
        if(updateUserDTO.getBio() != null && !updateUserDTO.getBio().isEmpty())
            user.setBio(updateUserDTO.getBio());
        if(updateUserDTO.getAvatarUrl() != null && !updateUserDTO.getAvatarUrl().isEmpty())
            user.setAvatarUrl(updateUserDTO.getAvatarUrl());
        updateById(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
