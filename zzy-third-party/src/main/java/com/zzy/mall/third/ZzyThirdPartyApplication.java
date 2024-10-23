package com.zzy.mall.third;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ZzyThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzyThirdPartyApplication.class, args);
    }

}
