package com.zzy.mall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.SkuInfoDao;
import com.zzy.mall.product.entity.SkuInfoEntity;
import com.zzy.mall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
                wrapper.eq(params.get("catelogId") != null && !params.get("catelogId").equals("0"),"catalog_id",params.get("catelogId"))
                        .eq(params.get("brandId") != null && !params.get("brandId").equals("0"),"brand_id",params.get("brandId"))
                        .ge(params.get("min") != null,"price",params.get("min"))
                        // max 要大于0 否则不作为检索条件
                        .le(params.get("max") != null && new BigDecimal((String) params.get("max")).compareTo(new BigDecimal(0)) == 1,"price",params.get("max"))
        );
        return new PageUtils(page);
    }

}