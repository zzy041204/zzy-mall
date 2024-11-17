package com.zzy.mall.auth.controller;

import com.zzy.mall.auth.feign.MemberFeignService;
import com.zzy.mall.auth.feign.ThirdFeignService;
import com.zzy.mall.auth.utils.RandomCodeUtils;
import com.zzy.mall.auth.vo.LoginVO;
import com.zzy.mall.auth.vo.UserRegisterVO;
import com.zzy.mall.common.constant.SMSConstant;
import com.zzy.mall.common.exception.BizCodeEnume;
import com.zzy.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/sms")
public class LoginController {

    @Autowired
    ThirdFeignService thirdFeignService;

    @Autowired
    MemberFeignService memberFeignService;
    
    @Autowired
    RedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sendCode")
    public R sendSmsCode(@RequestParam("phone") String phoneNum) {
        // 防止60秒之内重复发送
        String redisCode = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + phoneNum);
        if (StringUtils.isNotBlank(redisCode)){
            Long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time <= 60 * 1000){
                // 验证码发送间隔不足1分钟 返回提示
                return R.error(BizCodeEnume.VALID_SMS_EXCEPTION.getCode(),BizCodeEnume.VALID_SMS_EXCEPTION.getMsg());
            }
        }
        // 生成随机的验证码 --> 把生成的验证码存储到redis服务中 16671131204 123456
        String code = RandomCodeUtils.getRandomCode(5);
        Integer status = thirdFeignService.sendSmsCode(phoneNum, code);
        if (status == 200){
            code = code + "_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(SMSConstant.SMS_CODE_PREFIX+phoneNum,code,10, TimeUnit.MINUTES); //有效时间为十分钟
            return R.ok();
        }else{
            return R.error(BizCodeEnume.SEND_SMS_EXCEPTION.getCode(),BizCodeEnume.SEND_SMS_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVO vo, BindingResult bindingResult, Model model) {
        Map<String,String> map = new HashMap<>();
        if(bindingResult.hasErrors()){
            // 表示提交的数据不合法
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                String field = fieldError.getField();
                String message = fieldError.getDefaultMessage();
                map.put(field,message);
            }
            model.addAttribute("error",map);
            return "/reg";
        }else {
            // 验证码是否正确
            String code = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
            code = code.split("_")[0];
            if (!code.equals(vo.getCode())){
                // 说明验证码不正确
                map.put("code","验证码错误");
                model.addAttribute("error",map);
                return "/reg";
            }else {
                // 验证码正确 删除验证码
                redisTemplate.delete(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
                // 远程调用对应的服务 完成注册功能
                R register = memberFeignService.register(vo);
                if (register.getCode() == 0){
                    // 注册成功
                    return "redirect:http://auth.zzy.com/login.html";
                }else {
                    // 注册失败
                    map.put("message",register.getCode()+":"+register.get("msg"));
                    model.addAttribute("error",map);
                    return "/reg";
                }
            }
        }
    }

    /**
     * 注册的方法
     * @return
     */
    @PostMapping("/login")
    public String login(LoginVO vo, RedirectAttributes redirectAttributes){
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0){
            // 登录成功
            return "redirect:http://mall.zzy.com/home";
        }
        // 表示登录失败,重新跳转到登陆页面
        redirectAttributes.addAttribute("errors",login.get("msg"));
        return "redirect:http://auth.zzy.com/login.html";
    }

}
