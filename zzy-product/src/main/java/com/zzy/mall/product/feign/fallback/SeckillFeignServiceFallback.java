package com.zzy.mall.product.feign.fallback;

import com.zzy.mall.common.exception.BizCodeEnume;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getSeckillSessionBySkuId(Long skuId) {
        log.error("SeckillFeignService熔断降级...");
        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(),BizCodeEnume.UNKNOWN_EXCEPTION.getMsg());
    }
}
