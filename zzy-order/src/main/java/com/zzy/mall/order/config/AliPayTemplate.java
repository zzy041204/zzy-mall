package com.zzy.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.zzy.mall.order.vo.PayVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置文件
 */
//@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AliPayTemplate {

    // 商户appid
    public static String APPID = "9021000142624225";
    // 私钥 pkcs8格式的
    public static String RSA_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCZY08QxCjdFfh05MlRcEec3vepF/Rvar2yFQH2xfeglhDn7Dqt1uUN35QHTE6cOorA3QtSTRXKX6fKjLXWgnGe/ySK48ExLlJpDumUwo8hY43q1i5cK9T129e4qok0TW2xyz2FuOix4emqI1IG7PiLcCp5Eu9W7vAWwn0/xJgvDeqVBb+tEES+tGoZrECsyc+1fhrHaDrag2dWkmtsTxRmTDFdxJhjzZTqLqd6f/E5UTgXcqjT+F1V3onkLoQ6IlvJupky45xtt+OYZo8CodeTJ7hHANyl1NWJFqtKrhYpSuG12wJzYLtWHRGFP9PsP1iRH96aK2i6s0pPWJnMLc7ZAgMBAAECggEASoOI3E8+vrA2o6qk9fACif1y+G0PrLSA9Krp6lSfiB5+lfwXQW38lfG/+o+iPJjf4PNA1blm1YoEfAxBJbT2t1OhD+u2ZUroc+rvo/mkCIdRYPytRs9wRAOJV0dZIdRfbpiaCYUbXZauqZ1A8uZZk3to1qpRJL0sWxIeQEUj3O83+qFMrAw4sFjhmwdaRZu4FxlGXBq5iPIoqs/rrKyxO9PzwDhHfPdh4refmVLStqNMUIcGSxgtnZ7CzisW1Ma0mBkDzaSXZyDxKJgtfhit9mrkFZrEk6KnMRkyiDVmVW58+M4avF+WWhbrVGFQVIvF5WjGdUIb4/0wt9ddC/vmlQKBgQDQQypGqALSJ4A5omPRaXchQuQU17wgRvX9J5g19D/pnRpibrqPJXwwKa0bbFAFYw5eV17rE2NtanY5O2VfQmOTj+4n5dj4mwbdholJFNNz5hxt5ysMJfLdzqE2I1AVdJZqWjUBH+KTuur3pNfYnDvJiM0HKcmU1F2jhwOaSd/uiwKBgQC8jBygThjyxFlpSsjcxeqPvZ7wyRMzKQUsAMTxD0x2zxppm7Xjy3IjNZYa86P9/25Fq15xc15jwE4O1Bx7HJOZZZWzVgc20GGHSq3WvpBu7KpUH8IMXoj0ppIFp5Kib16DYHr/bsPfPX4DLK+CIu1rHLGpX0AWMElgFAE8fzNoqwKBgAEeWzvF6z31uFqW5LJbPqiPE2qJPhluSCPz8n1XsoAut9WPgrECIoifsK5VBpkAzzLyhS/+Cqqrx7bG+uRoFuDUBRNxyJiNPJwz/MjHs9sLgVuVwkVubr+CPJtt1SBIYAZyNZar9SuV9W0fFYm0TR3n/jHrp4mFGnumXf6WJv3BAoGBALquH0m5CYqUTdKqcOXmL6/SFyRgcdodqzk+KcBjXVnlBEm8GgNI0+F4lP5qbNx3oz7nxcKeb75kEOUtrmVWje07X1UIwGvNNkM19ZjBZU/uk1Zne0Gz9/YDVmCy4cRicDmztRRjHxxRTgrmm39GBbwf1OfVnyZY+M1gATlc2BGxAoGAHdg182Q2iN4/D2OixQeIPTH0wxaEV+eVv+2W/hLsMP54ZitZKXZ0L5H8NZdTUa6SG9fi8DpVK3g8JeLGvGxFrLkTzaMHmzaCQgrSue/MVNiSjyMHs4Xbz2aWBpCLZqFwYjHFAzYOkPc7Xlyy/U7uMwJl+F0ywsMoBs7viRmyB3s=";
    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://order.zzy.com/payed/notify";
    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String return_url = "http://order.zzy.com/orderPay/returnUrl";
    // 请求网关地址
    public static String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 编码
    public static String CHARSET = "UTF-8";
    // 返回格式
    public static String FORMAT = "json";
    // 支付宝公钥
    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl1eb6UDr++tJe26Da5P/5Yz0xfWpjbRR4d41vuCTu0TlG+qvcv8GJSTZg7EiZAys3OzrgV6ialKEaYTJ54hvtspHwtEjjpddq0G3B7LTEtG4k+RKLr16/MmpXrdOym8yw2SNdfzyZgAINmJsfs5Gn18uDoyzfkdCvo1lgYz7A+J800ybpG8NFMZb2S9zFy4MUuQPtaCYaAF2e5gbdFWMoDTERLnOdY/fTBi7nU/wBUzEGKjRPRjgbIG/ytfQhuxLLMLsSZHqgnzYilTdCskbGO31/KeVDoovFg3v6O161fJ4P1ikhhsq2bVTkU2Q+fp0KRyBYQi2EPM5tTtTtjtwiQIDAQAB";
    // 日志记录目录
    public static String log_path = "/log";
    // RSA2
    public static String SIGNTYPE = "RSA2";

    public String pay(PayVO payVO){
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        //调用RSA签名方式
        AlipayClient client = new DefaultAlipayClient(URL, APPID, RSA_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY,SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request=new AlipayTradeWapPayRequest();

        // 封装请求支付信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setOutTradeNo(payVO.getOut_order_no());
        model.setSubject(payVO.getSubject());
        model.setTotalAmount(payVO.getTotal_amount());
        model.setBody(payVO.getTotal_amount());
        model.setTimeoutExpress("5000");
        model.setProductCode("11111");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(notify_url);
        // 设置同步地址
        alipay_request.setReturnUrl(return_url);

        // form表单生产
        String form = "";
        try {
            // 调用SDK生成表单
            form = client.pageExecute(alipay_request).getBody();
            return form;
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}
