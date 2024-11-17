package com.zzy.mall.auth.feign;

import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "zzy-third")
public interface ThirdFeignService {

    @GetMapping("/third/sendCode")
    public Integer sendSmsCode(@RequestParam("phoneNum") String phoneNum, @RequestParam("code") String code);

}
