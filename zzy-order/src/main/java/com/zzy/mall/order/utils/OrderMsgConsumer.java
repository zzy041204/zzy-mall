package com.zzy.mall.order.utils;

import com.zzy.mall.common.constant.OrderConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@RocketMQMessageListener(topic = OrderConstant.ORDER_ROCKETMQ_TOPIC,consumerGroup = "${rocketmq.consumer.group}")
@Component
public class OrderMsgConsumer implements RocketMQListener<String> {


    @Override
    public void onMessage(String s) {
        // TODO 订单关单的逻辑操作
        System.out.println("收到的消息是：" + s);
    }
}
