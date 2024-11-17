package com.zzy.mall.third.utils;

import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信组件
 */
@ConfigurationProperties("spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String method = "POST";
    private String appcode;

    /**
     * 发送短信验证码
     * @param phoneNumber 发送的手机号
     * @param code 发送的短信验证码
     */
    public Integer sendSmsCode(String phoneNumber, String code) {
        /*String host = "https://wwsms.market.alicloudapi.com";
        String path = "/send_sms";
        String method = "POST";
        String appcode = "0de38643bd414257801a01c957337d7b";*/
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:"+code);
        bodys.put("template_id", "wangweisms996");   //注意，模板wangweisms996 仅作调试使用，下发短信不稳定，请联系客服报备自己的专属签名模板，以保障业务稳定使用
        bodys.put("phone_number", phoneNumber);

//可以提交工单联系客服，或者钉钉联系，钉钉号：1ko_t720ssqc54
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            return response.getStatusLine().getStatusCode();
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
