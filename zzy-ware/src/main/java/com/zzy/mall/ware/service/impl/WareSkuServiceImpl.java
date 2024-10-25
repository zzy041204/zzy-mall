package com.zzy.mall.ware.service.impl;

import com.zzy.mall.ware.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.ware.dao.WareSkuDao;
import com.zzy.mall.ware.entity.WareSkuEntity;
import com.zzy.mall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper.eq(wareId != null && wareId != "","ware_id", wareId)
                        .eq(skuId != null && skuId != "","sku_id", skuId)
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity wareSkuEntity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId).eq("ware_id", wareId));
        if (wareSkuEntity != null){
            // 在已有的库存信息里面添加库存数量
            Integer stock = wareSkuEntity.getStock();
            wareSkuEntity.setStock( stock + skuNum);
            this.updateById(wareSkuEntity);
        }else {
            // 新增一条库存信息
            WareSkuEntity skuEntity = new WareSkuEntity();
            String skuName = productFeignService.infoNameById(skuId);
            skuEntity.setSkuId(skuId);
            skuEntity.setWareId(wareId);
            skuEntity.setStock(skuNum);
            skuEntity.setSkuName(skuName);
            skuEntity.setStockLocked(0);
            this.save(skuEntity);
        }
    }

}