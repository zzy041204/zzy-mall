package com.zzy.mall.common.dto.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品上架时 需要保存到ES中的信息
 */
@Data
public class SkuESModel {

    private Long skuId;
    private Long spuId;
    private String subTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long categoryId;
    private String brandName;
    private String brandImg;
    private String categoryName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
