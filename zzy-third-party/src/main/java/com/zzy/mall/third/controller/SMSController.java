package com.zzy.mall.third.controller;

import com.zzy.mall.common.utils.R;
import com.zzy.mall.third.utils.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SMSController {

    @Autowired
    private SmsComponent smsComponent;

    /**
     * 调用服务商提供的API发送短信
     * @param phoneNum
     * @param code
     * @return
     */
    @GetMapping("/third/sendCode")
    public Integer sendSmsCode(@RequestParam("phoneNum") String phoneNum,@RequestParam("code") String code) {
        Integer status = smsComponent.sendSmsCode(phoneNum, code);
        return status;
    }

}
