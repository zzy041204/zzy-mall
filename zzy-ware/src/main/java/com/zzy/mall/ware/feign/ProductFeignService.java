package com.zzy.mall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "zzy-product")
public interface ProductFeignService {

    @PostMapping("product/skuinfo/info")
    public String infoNameById(@RequestBody Long skuId);

}
