package com.zzy.mall.order.vo;

import com.zzy.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderResponseVO {

    private OrderEntity orderEntity;

    private Integer code; // 0 表示成功 其他表示失败

}
