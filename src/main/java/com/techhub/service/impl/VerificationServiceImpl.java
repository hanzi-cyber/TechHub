package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.Result;
import com.techhub.common.ResultCode;
import com.techhub.common.exception.BusinessException;
import com.techhub.dto.LoginDTO;
import com.techhub.dto.RegisterDTO;
import com.techhub.entity.User;
import com.techhub.mapper.UserMapper;
import com.techhub.service.IVerificationService;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.techhub.common.ResultCode.USER_NOT_FOUND;

@Service
public class VerificationServiceImpl extends ServiceImpl<UserMapper, User> implements IVerificationService {


    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @Override
    public UserVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        //根据用户名查询用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        //校验密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        //校验状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        //将用户信息转换成UserVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 注册
     * @param registerDTO
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        User user = new User();
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String email = registerDTO.getEmail();
        //校验邮箱是否存在
        if(this.getOne(new QueryWrapper<User>().eq("email", email)) != null) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }
        //校验用户名是否存在
        if(this.getOne(new QueryWrapper<User>().eq("username", username)) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        BeanUtils.copyProperties(registerDTO, user);
        //加密密码
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        //设置状态
        user.setStatus(1);
        //保存用户
        this.save(user);
    }
}
