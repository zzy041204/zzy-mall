package com.zzy.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.zzy.mall.ware.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.zzy.mall.ware.dao")
public class ZzyWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzyWareApplication.class, args);
    }

}
