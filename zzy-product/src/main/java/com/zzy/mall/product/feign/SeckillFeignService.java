package com.zzy.mall.product.feign;

import com.zzy.mall.common.utils.R;
import com.zzy.mall.product.feign.fallback.SeckillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "zzy-seckill",fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {

    @GetMapping("/seckill/seckillSessionBySkuId")
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId);

}
