package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.SpuInfoEntity;
import com.zzy.mall.product.vo.OrderItemSpuInfoVO;
import com.zzy.mall.product.vo.SpuInfoVO;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuInfoVO spuInfoVO);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuIds(Long[] spuIds);
}

