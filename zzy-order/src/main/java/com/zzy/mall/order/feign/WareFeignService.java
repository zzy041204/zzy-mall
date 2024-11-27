package com.zzy.mall.order.feign;

import com.zzy.mall.common.utils.R;
import com.zzy.mall.order.vo.WareSkuLockVO;
import com.zzy.mall.order.vo.WareSkuReduceVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "zzy-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVO vo);

    @PostMapping("/ware/waresku/reduce")
    public R reduceStock(@RequestBody List<WareSkuReduceVO> list);

}
