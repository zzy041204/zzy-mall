package com.zzy.mall.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundsDTO {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}
