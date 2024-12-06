package com.zzy.mall.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillSkuRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	private Long promotionId;

	private Long promotionSessionId;

	private Long skuId;

	private BigDecimal seckillPrice;

	private BigDecimal seckillCount;

	private BigDecimal seckillLimit;

	private Integer seckillSort;

}
