/**
  * Copyright 2024 bejson.com 
  */
package com.zzy.mall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

/**
 * Auto-generated: 2024-10-23 15:32:17
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Skus {

    private List<Attr> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private Integer fullCount;
    private BigDecimal discount;
    private Integer countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer priceStatus;
    private List<MemberPrice> memberPrice;

}