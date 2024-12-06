package com.zzy.mall.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderDTO {

    private String orderSn;

    private String skuId;

    private String promotionSessionId;

    private BigDecimal seckillPrice; // 秒杀价格

    private Integer num;

    private Long memberId;

}
