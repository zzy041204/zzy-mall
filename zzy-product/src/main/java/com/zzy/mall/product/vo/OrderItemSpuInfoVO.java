/**
  * Copyright 2024 bejson.com 
  */
package com.zzy.mall.product.vo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2024-10-23 15:32:17
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class OrderItemSpuInfoVO implements Serializable {

    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private String catalogName;
    private Long brandId;
    private String brandName;
    private String img;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}