package com.moyu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.moyu.mapper")
@SpringBootApplication
public class MoyuTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoyuTestApplication.class, args);
    }

}
