package com.zzy.mall.order.feign;

import com.zzy.mall.common.utils.R;
import com.zzy.mall.order.vo.OrderItemSpuInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @FeignClient 指明我们从注册中心发现的服务的名称
 */
@FeignClient(name = "zzy-product")
public interface ProductFeignService {

    /**
     * 需要调用的远程方法
     * @return
     */
    @GetMapping("/product/brand/all")
    public R queryAllBrand();

    @RequestMapping("/product/spuinfo/getOrderItemSpuInfoBySpuId/{spuIds}")
    public List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(@PathVariable("spuIds") Long[] spuIds);

}
