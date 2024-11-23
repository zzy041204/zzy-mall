package com.zzy.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableCaching // 放开缓存
@EnableFeignClients(basePackages = "com.zzy.mall.product.feign")
@EnableDiscoveryClient //放开注册中心
@SpringBootApplication(scanBasePackages = "com.zzy.mall")
@MapperScan("com.zzy.mall.product.dao")
public class ZzyProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZzyProductApplication.class,args);
    }
}
