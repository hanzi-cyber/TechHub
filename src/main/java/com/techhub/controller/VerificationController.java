package com.techhub.controller;

import com.techhub.common.Constant;
import com.techhub.common.Result;
import com.techhub.common.properties.JwtProperties;
import com.techhub.context.BaseContext;
import com.techhub.dto.LoginDTO;
import com.techhub.dto.RegisterDTO;
import com.techhub.service.ILoginSessionService;
import com.techhub.service.IVerificationService;
import com.techhub.utils.JwtUtil;
import com.techhub.vo.LoginVO;
import com.techhub.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class VerificationController {

    @Autowired
    private IVerificationService verificationService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ILoginSessionService loginSessionService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO){
        UserVO user = verificationService.login(loginDTO);
        //生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );
        // 登录态写入 Redis(带过期时间),用于分布式会话 + 单点登录(新登录覆盖旧 token)
        loginSessionService.saveToken(user.getId(), token);

        LoginVO loginVO = new LoginVO();
        loginVO.setUser(user);
        loginVO.setToken(token);
        return Result.success(loginVO);
    }
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO){
        verificationService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(){
        // 删除登录态,使 token 立即失效(分布式会话下所有节点共享同一份会话)
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            loginSessionService.removeToken(userId);
        }
        return Result.success();
    }
}











