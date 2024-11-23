package com.zzy.mall.order.vo;

import lombok.Data;

/**
 * 订单结算页提交的信息
 */
@Data
public class OrderSubmitVO {

    // 收获地址的id
    private Long addrId;

    // 支付方式
    private Integer payType;

    // 防止重复提交 token
    private String orderToken;

    // 买家备注
    private String note;

}
