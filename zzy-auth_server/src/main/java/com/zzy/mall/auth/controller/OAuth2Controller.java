package com.zzy.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.zzy.mall.auth.feign.MemberFeignService;
import com.zzy.mall.auth.vo.SocialUser;
import com.zzy.mall.common.utils.HttpUtils;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.common.vo.MemberVO;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @RequestMapping("/oauth/weibo/success")
    public String weiboAuth(@RequestParam("code") String code,
                            HttpSession session,
                            HttpServletResponse response) throws Exception {
        // 根据code 获取token信息
        HashMap<String, String> body = new HashMap<>();
        body.put("client_id", "2321701720");
        body.put("client_secret", "b1f0f981f5210a97f439293f70e703c0");
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", "http://auth.zzy.com/oauth/weibo/success");
        body.put("code", code);
        // 根据Code获取对应的Token信息
        HttpResponse post = HttpUtils.doPost("https://api.weibo.com",
                "/oauth2/access_token",
                "post",
                new HashMap<>(),
                null,
                body);
        int statusCode = post.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            // 说明获取Token失败，调回到登录页面
            return "redirect:http://auth.zzy.com/login.html";
        }else {
            // 注册成功后跳转到商城首页
            String json = EntityUtils.toString(post.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            R r = memberFeignService.socialLogin(socialUser);
            if (r.getCode() != 0) {
                // 登录错误
                return "redirect:http://auth.zzy.com/login.html";
            }else {
                String entityJson = (String) r.get("entity");
                System.out.println("----------------------------------->"+entityJson);
                MemberVO memberVO = JSON.parseObject(entityJson, MemberVO.class);
                session.setAttribute("loginUser", memberVO);
                return "redirect:http://mall.zzy.com/home";
            }
        }
    }

}
