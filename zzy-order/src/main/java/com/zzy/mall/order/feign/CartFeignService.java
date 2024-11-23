package com.zzy.mall.order.feign;

import com.zzy.mall.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(name = "zzy-cart")
public interface CartFeignService {

    @GetMapping("/getUserItems")
    @ResponseBody
    public List<OrderItemVO> getUserCartItems();

}
