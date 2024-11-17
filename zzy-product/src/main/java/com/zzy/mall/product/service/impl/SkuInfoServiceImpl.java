package com.zzy.mall.product.service.impl;

import com.zzy.mall.product.entity.*;
import com.zzy.mall.product.service.*;
import com.zzy.mall.product.vo.ItemVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.SkuInfoDao;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * SKU 信息检索
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        // 检索关键字
        if (StringUtils.isNotBlank(key)){
            wrapper.and( w -> {
                    w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                // 分类 品牌 价格区间
                wrapper.eq(catelogId != null && !catelogId.equals("0"),"catalog_id",catelogId)
                        .eq(brandId != null && !brandId.equals("0"),"brand_id",brandId)
                        .ge(min != null,"price",min)
                        // max 要大于0 否则不作为检索条件
                        .le(max != null && new BigDecimal(max).compareTo(new BigDecimal(0)) == 1,"price",max)
        );
        return new PageUtils(page);
    }

    @Override
    public ItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        ItemVO itemVO = new ItemVO();
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            // 1.skuId的基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            if (skuInfoEntity != null) {
                itemVO.setInfo(skuInfoEntity);
            }
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 3.获取sku中的销售属性的组合
            List<Long> skuIds = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", skuInfoEntity.getSpuId())).stream().map(
                    s -> {
                        return s.getSkuId();
                    }
            ).collect(Collectors.toList());
            List<ItemVO.SkuItemSaleVO> saleVOList = skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>()
                    .select("attr_id", "attr_name", "GROUP_CONCAT(DISTINCT attr_value) as group_attr_value")
                    .in("sku_id", skuIds)
                    .groupBy("attr_id", "attr_name")).stream().map(
                    s -> {
                        ItemVO.SkuItemSaleVO skuItemSaleVO = new ItemVO.SkuItemSaleVO();
                        skuItemSaleVO.setAttrId(s.getAttrId());
                        skuItemSaleVO.setAttrName(s.getAttrName());
                        skuItemSaleVO.setAttrValue(s.getGroupAttrValue());
                        return skuItemSaleVO;
                    }
            ).collect(Collectors.toList());
            if (saleVOList != null && !saleVOList.isEmpty()) {
                itemVO.setSaleAttrs(saleVOList);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> spuFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 4.获取SPU的介绍
            SpuInfoDescEntity descEntity = spuInfoDescService.getById(skuInfoEntity.getSpuId());
            if (descEntity != null) {
                itemVO.setDesc(descEntity);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> groupFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 5.获取SPU的规格参数
            // 根据类别号找到属性组
            List<ItemVO.SpuItemGroupAttrVO> spuItemGroupAttrVOS = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", skuInfoEntity.getCatalogId())).stream().map(
                    attrGroupEntity -> {
                        ItemVO.SpuItemGroupAttrVO spuItemGroupAttrVO = new ItemVO.SpuItemGroupAttrVO();
                        //设置属性组名
                        spuItemGroupAttrVO.setGroupName(attrGroupEntity.getAttrGroupName());
                        // 找到属性组关联的规格属性
                        List<Long> attrIds = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId())).stream().map(
                                attrAttrgroupRelationEntity -> {
                                    return attrAttrgroupRelationEntity.getAttrId();
                                }
                        ).collect(Collectors.toList());
                        List<ItemVO.SpuItemBaseAttrVO> baseAttrVOS = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", skuInfoEntity.getSpuId()).in("attr_id", attrIds)).stream().map(
                                productAttrValueEntity -> {
                                    ItemVO.SpuItemBaseAttrVO spuItemBaseAttrVO = new ItemVO.SpuItemBaseAttrVO();
                                    spuItemBaseAttrVO.setAttrName(productAttrValueEntity.getAttrName());
                                    spuItemBaseAttrVO.setAttrValue(productAttrValueEntity.getAttrValue());
                                    return spuItemBaseAttrVO;
                                }
                        ).collect(Collectors.toList());
                        spuItemGroupAttrVO.setBaseAttrVOs(baseAttrVOS);
                        return spuItemGroupAttrVO;
                    }
            ).collect(Collectors.toList());
            if (spuItemGroupAttrVOS != null && !spuItemGroupAttrVOS.isEmpty()) {
                itemVO.setBaseAttrs(spuItemGroupAttrVOS);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2.sku的图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            if (skuImagesEntities != null && !skuImagesEntities.isEmpty()) {
                itemVO.setImages(skuImagesEntities);
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(saleFuture, spuFuture, groupFuture,imageFuture).get();

        return itemVO;
    }

    @Override
    public List<String> getSkuSaleAttrs(Long skuId) {
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>()
                .select("CONCAT(attr_name,':',attr_value) AS concatString").eq("sku_id", skuId));
        List<String> list = new ArrayList<>();
        if (skuSaleAttrValueEntityList != null && !skuSaleAttrValueEntityList.isEmpty()) {
            skuSaleAttrValueEntityList.forEach(skuSaleAttrValueEntity -> {
                list.add(skuSaleAttrValueEntity.getConcatString());
            });
        }
        return list;
    }

}