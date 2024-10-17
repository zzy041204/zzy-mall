package com.zzy.mall.order.feign;

import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @FeignClient 指明我们从注册中心发现的服务的名称
 */
@FeignClient(name = "zzy-product")
public interface ProductService {

    /**
     * 需要调用的远程方法
     * @return
     */
    @GetMapping("/product/brand/all")
    public R queryAllBrand();

}
