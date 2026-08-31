package com.techhub.config;

import com.techhub.interceptor.JwtTokenUserInterceptor;
import com.techhub.utils.StringToSortTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置:注册拦截器 + 自定义参数转换器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private StringToSortTypeConverter stringToSortTypeConverter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenUserInterceptor)
                // 拦截所有 /api/** 请求
                .addPathPatterns("/api/**")
                // 登录、注册不需要 token
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    /**
     * 注册自定义参数转换器:把 "latest"/"hot" 字符串转成 SortType 枚举
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToSortTypeConverter);
    }
}
