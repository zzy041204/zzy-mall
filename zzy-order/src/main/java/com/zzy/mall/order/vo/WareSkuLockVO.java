package com.zzy.mall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVO {

    // 订单编号
    private String orderSn;

    private List<OrderItemVO> orderItems;

}
