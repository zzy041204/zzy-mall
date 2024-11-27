package com.zzy.mall.order.utils;

import com.zzy.mall.common.constant.OrderConstant;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderMsgProducer {

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    public void sendOrderMessage(String orderSn){
        rocketMQTemplate.syncSend(OrderConstant.ORDER_ROCKETMQ_TOPIC,
                MessageBuilder.withPayload(orderSn).build(),5000,4);
    }

}
