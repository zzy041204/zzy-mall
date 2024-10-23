package com.zzy.mall.product.feign;

import com.zzy.mall.common.dto.SkuReductionDTO;
import com.zzy.mall.common.dto.SpuBoundsDTO;
import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "zzy-coupon")
public interface CouponFeignService {

    @PostMapping("coupon/skufullreduction/saveinfo")
    public R saveFullReductionInfo(@RequestBody SkuReductionDTO dto);

    /**
     * 保存
     */
    @RequestMapping("coupon/spubounds/saveSpuBounds")
    public R saveSpuBounds(@RequestBody SpuBoundsDTO spuBounds);

}
