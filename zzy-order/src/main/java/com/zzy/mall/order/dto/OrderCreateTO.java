package com.zzy.mall.order.dto;

import com.zzy.mall.order.entity.OrderEntity;
import com.zzy.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateTO {

    private OrderEntity orderEntity; // 订单信息

    private List<OrderItemEntity> orderItemEntity; // 订单项信息

}
