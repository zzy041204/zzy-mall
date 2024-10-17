package com.zzy.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.zzy.mall.member.dao")
public class ZzyMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzyMemberApplication.class, args);
    }

}
