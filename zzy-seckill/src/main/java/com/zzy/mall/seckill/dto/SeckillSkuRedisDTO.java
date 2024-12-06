package com.zzy.mall.seckill.dto;

import com.zzy.mall.seckill.vo.SkuInfoVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装Sku相关信息，保存到Redis中的传输对象
 */
@Data
public class SeckillSkuRedisDTO {

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;

    private SkuInfoVO skuInfoVO;

    private Long startTime;

    private Long endTime;

    // 随机码
    private String randomCode;

}
