package com.zzy.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.zzy.mall.order.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.zzy.mall.order.dao")
public class ZzyOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzyOrderApplication.class, args);
    }

}
