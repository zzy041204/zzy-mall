package com.zzy.mall.order.feign;

import com.zzy.mall.common.utils.R;
import com.zzy.mall.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "zzy-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    public List<MemberAddressVO> getAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/memberreceiveaddress/getAddressById/{id}")
    public MemberAddressVO getAddressById(@PathVariable("id") Long id);

}
