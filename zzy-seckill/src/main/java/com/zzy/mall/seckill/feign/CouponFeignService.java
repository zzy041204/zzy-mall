package com.zzy.mall.seckill.feign;

import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "zzy-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getLater3DaysSession")
    public R getLater3DaysSession();

}
