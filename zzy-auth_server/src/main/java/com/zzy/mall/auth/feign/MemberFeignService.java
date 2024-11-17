package com.zzy.mall.auth.feign;

import com.zzy.mall.auth.vo.LoginVO;
import com.zzy.mall.auth.vo.UserRegisterVO;
import com.zzy.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 会员服务
 */
@FeignClient(name = "zzy-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    public R register(@RequestBody UserRegisterVO vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody LoginVO vo);

}
