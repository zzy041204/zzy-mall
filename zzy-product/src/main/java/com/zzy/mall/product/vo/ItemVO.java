package com.zzy.mall.product.vo;

import com.zzy.mall.product.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 商品详情页的数据对象
 */
@Data
public class ItemVO {
    // 1.skuId的基本信息 pms_sku_info
    private SkuInfoEntity info;
    private boolean hasStock = true; // 是否有库存
    // 2.sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;
    // 3.获取sku中的销售属性的组合
    private List<SkuItemSaleVO> saleAttrs;
    // 4.获取SPU的介绍
    private SpuInfoDescEntity desc;
    // 5.获取SPU的规格参数
    private List<SpuItemGroupAttrVO> baseAttrs;

    private SeckillVO seckillVO;

    @Data
    public static class SkuItemSaleVO{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

    @Data
    public static class SpuItemGroupAttrVO{
        private String groupName;
        private List<SpuItemBaseAttrVO> baseAttrVOs;
    }

    @Data
    public static class SpuItemBaseAttrVO{
        private String attrName;
        private String attrValue;
    }

}
