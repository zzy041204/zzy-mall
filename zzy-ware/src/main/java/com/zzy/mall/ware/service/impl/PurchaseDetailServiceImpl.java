package com.zzy.mall.ware.service.impl;

import com.zzy.mall.ware.service.WareInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.ware.dao.PurchaseDetailDao;
import com.zzy.mall.ware.entity.PurchaseDetailEntity;
import com.zzy.mall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String status = (String) params.get("status");
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)){
            wrapper.and( w -> w.eq("sku_id", key).or()
                    .eq("purchase_id",key));
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper.eq(wareId != null && wareId != "","ware_id", wareId)
                        .eq(status != null && status != "","status",status)
        );
        return new PageUtils(page);
    }

}