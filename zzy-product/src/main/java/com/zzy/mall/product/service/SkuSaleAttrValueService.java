package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

