package com.zzy.mall.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SpuInfoResponseVO {

    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private String catalogName;
    private Long brandId;
    private String brandName;
    private BigDecimal weight;
    private Integer publishStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
    private Date updateTime;

}
