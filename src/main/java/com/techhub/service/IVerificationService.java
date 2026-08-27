package com.techhub.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.dto.LoginDTO;
import com.techhub.dto.RegisterDTO;
import com.techhub.entity.User;
import com.techhub.vo.UserVO;
import jakarta.validation.Valid;

public interface IVerificationService extends IService<User> {
    UserVO login(LoginDTO loginDTO);

    void register(@Valid RegisterDTO registerDTO);
}