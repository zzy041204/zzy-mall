package com.zzy.mall.cart.feign;

import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "zzy-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{id}")
    public R info(@PathVariable("id") Long id);

    @GetMapping("/product/skuinfo/saleAttrs/{skuId}")
    public List<String> getSkuSaleAttrs(@PathVariable("skuId") Long skuId);

}
