package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.SkuInfoEntity;
import com.zzy.mall.product.vo.ItemVO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    ItemVO item(Long skuId) throws ExecutionException, InterruptedException;
}

