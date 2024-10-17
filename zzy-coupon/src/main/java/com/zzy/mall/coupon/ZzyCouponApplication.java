package com.zzy.mall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.zzy.mall.coupon.dao")
public class ZzyCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzyCouponApplication.class, args);
    }

}
