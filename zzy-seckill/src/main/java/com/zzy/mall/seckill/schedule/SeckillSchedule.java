package com.zzy.mall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 定时任务
 * 1.@EnableScheduling 开启定时任务的注解
 * 2.@Scheduled 具体开启一个定时任务 通过corn来定时
 */
@Slf4j
@Component
public class SeckillSchedule {

    /**
     * 默认情况的定时任务是一个同步的任务 需要让它异步处理
     * 1.我们把需要定时执行的任务交给异步处理器来处理
     * 2.我们需要执行的方法异步执行
     *   @EnableAsync 开启异步任务的功能
     *   @Async 希望异步执行的方法
     */
    @Async
    @Scheduled(cron = "0 0 2 * * *")
    public void seckill() {
        log.info("定时任务测试");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
