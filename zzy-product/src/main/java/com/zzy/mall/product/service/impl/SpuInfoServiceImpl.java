package com.zzy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zzy.mall.common.constant.ProductConstant;
import com.zzy.mall.common.dto.MemberPrice;
import com.zzy.mall.common.dto.SkuReductionDTO;
import com.zzy.mall.common.dto.SkuStockDTO;
import com.zzy.mall.common.dto.SpuBoundsDTO;
import com.zzy.mall.common.dto.es.SkuESModel;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.product.entity.*;
import com.zzy.mall.product.feign.CouponFeignService;
import com.zzy.mall.product.feign.SearchFeignService;
import com.zzy.mall.product.feign.WareFeignService;
import com.zzy.mall.product.service.*;
import com.zzy.mall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Autowired
    CategoryService categoryService;

    @Autowired
    BrandService brandService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

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
     *
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
        spuInfoEntity.setPublishStatus(0); // 设置状态为新建状态
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
            skus.forEach((item) -> {
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
                    if (images2.getDefaultImg() == 1) {
                        // 表示是默认图片
                        defaultImg = images2.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoEntity.setSkuDesc(String.join(",", item.getDescar()));
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
                if (item.getMemberPrice() != null && item.getMemberPrice().size() > 0) {
                    List<MemberPrice> list = item.getMemberPrice().stream().map(memberPrice -> {
                        MemberPrice mDto = new MemberPrice();
                        BeanUtils.copyProperties(memberPrice, mDto);
                        return mDto;
                    }).collect(Collectors.toList());
                    dto.setMemberPrice(list);
                }
                R r = couponFeignService.saveFullReductionInfo(dto);
                if (r.getCode() != 0) {
                    log.error("调用Coupon服务处理满减、折扣、会员价失败...");
                }
                // 5.4 sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> saleAttrs = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrs.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
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
        if (r.getCode() != 0) {
            log.error("调用Coupon服务存储积分信息操作失败");
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String status = (String) params.get("status");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key).or().like("spu_description", key);
            });
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper.eq(catelogId != null && !catelogId.equals("0"), "catalog_id", catelogId)
                        .eq(brandId != null && !brandId.equals("0"), "brand_id", brandId)
                        .eq(status != null, "publish_status", status)
        );
        //根据查询到的分页信息,在查询出对应的类别名称和品牌名称
        List<SpuInfoResponseVO> spuInfoResponseVOS = page.getRecords().stream().map(s -> {
            SpuInfoResponseVO spuInfoResponseVO = new SpuInfoResponseVO();
            BeanUtils.copyProperties(s, spuInfoResponseVO);
            Long catalogId = s.getCatalogId();
            Long brandId1 = s.getBrandId();
            CategoryEntity categoryEntity = categoryService.getById(catalogId);
            spuInfoResponseVO.setCatalogName(categoryEntity.getName());
            BrandEntity brandEntity = brandService.getById(brandId1);
            spuInfoResponseVO.setBrandName(brandEntity.getName());
            return spuInfoResponseVO;
        }).collect(Collectors.toList());
        return new PageUtils(spuInfoResponseVOS, (int) page.getTotal(), (int) page.getSize(), (int) page.getCurrent());
    }

    /**
     * 实现商品上架 商品相关数据存储到ElasticSearch中
     * 1.根据spuId查询出相关的信息 封装到对应的对象中
     * 2.将封装的对象存储到ElasticSearch中 调用zzy-search服务
     * 3.更新spuId对应的状态
     *
     * @param spuId
     */
    @Transactional
    @Override
    public void up(Long spuId) {
        //1.根据spuId查询出相关的信息 封装到SkuESModel对象中
        List<SkuESModel> skuEs = new ArrayList<>();
        List<SkuESModel.Attrs> attrs = getAttrs(spuId);
        // 根据spuId找到相关sku信息
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        List<Long> skuIds = skuInfoEntityList.stream().map(skuInfoEntity -> skuInfoEntity.getSkuId()).collect(Collectors.toList());
        Map<Long, Boolean> hasStockMap = getSkusHasStock(skuIds);
        skuInfoEntityList.stream().forEach(skuInfoEntity -> {
            SkuESModel skuESModel = new SkuESModel();
            // spu相关信息
            skuESModel.setSpuId(spuId);
            // sku相关信息
            skuESModel.setSkuId(skuInfoEntity.getSkuId());
            skuESModel.setSkuPrice(skuInfoEntity.getPrice());
            skuESModel.setSubTitle(skuInfoEntity.getSkuTitle());
            skuESModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            skuESModel.setSaleCount(skuInfoEntity.getSaleCount());
            //类别和品牌相关信息
            CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
            skuESModel.setCategoryId(categoryEntity.getCatId());
            skuESModel.setCategoryName(categoryEntity.getName());
            BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
            skuESModel.setBrandId(brandEntity.getBrandId());
            skuESModel.setBrandName(brandEntity.getName());
            skuESModel.setBrandImg(brandEntity.getLogo());
            // 规格参数相关信息
            skuESModel.setAttrs(attrs);
            // 库存相关信息
            if (hasStockMap == null){
                skuESModel.setHasStock(true);
            }else {
                skuESModel.setHasStock(hasStockMap.get(skuInfoEntity.getSkuId()));
            }
            // 热度分默认为0
            skuESModel.setHotScore(0l);
            skuEs.add(skuESModel);
        });
        // 2.将封装的对象存储到ElasticSearch中 调用zzy-search服务
        R r = searchFeignService.productStatusUp(skuEs);
        if (r.getCode() == 0) {
            // 远程调用失败
            // 3.更新spuId对应的状态
            this.update(new UpdateWrapper<SpuInfoEntity>().set("publish_status", ProductConstant.StatusEnum.SPU_PU.getCode()).set("update_time",new Date()).eq("id", spuId));
        }
    }

    /**
     * 根据skuIds获取对应的库存状态
     * @param skuIds
     * @return
     */
    private Map<Long, Boolean> getSkusHasStock(List<Long> skuIds) {
        List<SkuStockDTO> skuStockDTOS = null;
        if (skuIds == null || skuIds.size() == 0) {
            return null;
        }
        try {
            skuStockDTOS = wareFeignService.HasStock(skuIds);
            Map<Long, Boolean> map = skuStockDTOS.stream().collect(Collectors.toMap(item -> item.getSkuId()
                    , item -> item.getHasStock()
            ));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据spuId获取对应的规格参数
     *
     * @param spuId
     * @return
     */
    private List<SkuESModel.Attrs> getAttrs(Long spuId) {
        // 规格参数列表
        List<SkuESModel.Attrs> attrs = new ArrayList<>();
        // product_attr_value 存储了对应的spu相关的所有规格参数
        List<ProductAttrValueEntity> productAttrValueEntityList = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        productAttrValueEntityList.stream().forEach(productAttrValueEntity -> {
            Long attrId = productAttrValueEntity.getAttrId();
            // attr search_type 决定了该属性是否支持检索
            AttrEntity attrEntity = attrService.getById(attrId);
            if (attrEntity != null && attrEntity.getSearchType() == 1) {
                // 将可检索的属性添加到列表中
                SkuESModel.Attrs attr = new SkuESModel.Attrs();
                BeanUtils.copyProperties(productAttrValueEntity, attr);
                attrs.add(attr);
            }
        });
        return attrs;
    }

}