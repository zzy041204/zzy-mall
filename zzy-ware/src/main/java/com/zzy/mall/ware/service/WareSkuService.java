package com.zzy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.dto.SkuStockDTO;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.ware.entity.WareSkuEntity;
import com.zzy.mall.ware.vo.LockStockResult;
import com.zzy.mall.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:18:29
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuStockDTO> HasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVO vo);
}

