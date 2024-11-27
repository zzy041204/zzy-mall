package com.zzy.mall.order.vo;

import lombok.Data;

/**
 * 封装支付需要的相关信息
 */
@Data
public class PayVO {
    // 商户订单号
    private String out_order_no;
    // 订单名称
    private String subject;
    // 付款金额
    private String total_amount;
    // 描述
    private String body;
}
