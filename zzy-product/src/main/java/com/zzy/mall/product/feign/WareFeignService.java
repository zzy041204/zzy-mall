package com.zzy.mall.product.feign;

import com.zzy.mall.common.dto.SkuStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "zzy-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    public List<SkuStockDTO> HasStock(@RequestBody List<Long> skuIds);

}
