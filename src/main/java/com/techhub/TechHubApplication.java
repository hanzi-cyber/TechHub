package com.techhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 */
@SpringBootApplication
@MapperScan("com.techhub.mapper")
public class TechHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechHubApplication.class, args);
    }
}
