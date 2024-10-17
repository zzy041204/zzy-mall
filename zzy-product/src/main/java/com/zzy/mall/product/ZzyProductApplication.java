package com.zzy.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//放开注册中心
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.zzy.mall")
@MapperScan("com.zzy.mall.product.dao")
public class ZzyProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZzyProductApplication.class,args);
    }
}
