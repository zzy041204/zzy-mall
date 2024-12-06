package com.zzy.mall.seckill.service;

import com.zzy.mall.seckill.dto.SeckillSkuRedisDTO;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSku3Days();

    List<SeckillSkuRedisDTO> getCurrentSeckillSkus();

    SeckillSkuRedisDTO getSeckillSessionBySkuId(Long skuId);

    String kill(String killId, String code, Integer num);
}
