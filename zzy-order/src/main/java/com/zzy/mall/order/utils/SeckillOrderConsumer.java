package com.zzy.mall.order.utils;

import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.constant.OrderConstant;
import com.zzy.mall.common.dto.SeckillOrderDTO;
import com.zzy.mall.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RocketMQMessageListener(topic = OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC,consumerGroup = "${rocketmq.consumer.group}")
@Component
public class SeckillOrderConsumer implements RocketMQListener<String> {

    @Autowired
    OrderService orderService;

    @Override
    public void onMessage(String s) {
        SeckillOrderDTO seckillOrderDTO = JSON.parseObject(s, SeckillOrderDTO.class);
        orderService.qiuckCreateOrder(seckillOrderDTO);
    }
}
