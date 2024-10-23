package com.zzy.mall.product.service.impl;

import com.zzy.mall.common.dto.MemberPrice;
import com.zzy.mall.common.dto.SkuReductionDTO;
import com.zzy.mall.common.dto.SpuBoundsDTO;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.product.entity.*;
import com.zzy.mall.product.feign.CouponFeignService;
import com.zzy.mall.product.service.*;
import com.zzy.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrService attrService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品的发布信息
     * @param spuInfoVO
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVO spuInfoVO) {
        // 1.保存spu的基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVO, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        // 2.保存spu详情信息及描述图片 pms_spu_info_desc
        Long id = spuInfoEntity.getId();
        List<String> decripts = spuInfoVO.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(id);
        spuInfoDescEntity.setDecript(String.join(",", decripts));
        spuInfoDescService.save(spuInfoDescEntity);
        // 3.保存spu的图片集信息 pms_spu_images
        List<String> images = spuInfoVO.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(i -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(id);
            spuImagesEntity.setImgUrl(i);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);
        // 4.保存规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuInfoVO.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            //对规格数据做对应的处理
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(id); // 关联商品编号
            productAttrValueEntity.setAttrId(attr.getAttrId()); //
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);
        // 5.保存当前的spu 对应的所有的sku
        List<Skus> skus = spuInfoVO.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach( (item) -> {
                // 5.1 保存sku的基本信息 pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(id);
                skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
                skuInfoEntity.setSaleCount(0l);
                List<Images> images1 = item.getImages();
                String defaultImg = "";
                for (Images images2 : images1) {
                    if (images2.getDefaultImg() == 1){
                        // 表示是默认图片
                        defaultImg = images2.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoEntity.setSkuDesc(String.join(",",item.getDescar()));
                skuInfoService.save(skuInfoEntity);
                // 5.2 保存sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntities = images1.stream().map(i -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(i.getImgUrl());
                    skuImagesEntity.setDefaultImg(i.getDefaultImg());
                    return skuImagesEntity;
                }).filter(img -> {
                    return img.getImgUrl() != "" && img.getImgUrl() != null;
                }).collect(Collectors.toList()); // todo 为空的图片不需要保存
                skuImagesService.saveBatch(skuImagesEntities);
                // 5.3 保存满减信息 折扣 会员价 mall-sms: sms_sku_ladder sms_full_reduction sms_member_price
                SkuReductionDTO dto = new SkuReductionDTO();
                BeanUtils.copyProperties(item, dto);
                dto.setSkuId(skuInfoEntity.getSkuId());
                // 设置会员价
                if (item.getMemberPrice() != null && item.getMemberPrice().size() > 0){
                    List<MemberPrice> list = item.getMemberPrice().stream().map(memberPrice -> {
                        MemberPrice mDto = new MemberPrice();
                        BeanUtils.copyProperties(memberPrice, mDto);
                        return mDto;
                    }).collect(Collectors.toList());
                    dto.setMemberPrice(list);
                }
                R r = couponFeignService.saveFullReductionInfo(dto);
                if (r.getCode() != 0){
                    log.error("调用Coupon服务处理满减、折扣、会员价失败...");
                }
                // 5.4 sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> saleAttrs = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrs.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    BeanUtils.copyProperties(a,skuSaleAttrValueEntity);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);
            });
        }
        //6.保存spu的积分信息:mall-sms: sms_spu_bounds
        Bounds bounds = spuInfoVO.getBounds();
        SpuBoundsDTO spuBoundsDTO = new SpuBoundsDTO();
        BeanUtils.copyProperties(bounds, spuBoundsDTO);
        spuBoundsDTO.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsDTO);
        if (r.getCode() != 0){
            log.error("调用Coupon服务存储积分信息操作失败");
        }
    }

}