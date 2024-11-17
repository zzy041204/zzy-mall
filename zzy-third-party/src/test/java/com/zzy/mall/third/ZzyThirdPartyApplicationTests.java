package com.zzy.mall.third;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.zzy.mall.third.utils.SmsComponent;
import org.apache.http.HttpResponse;
import com.zzy.mall.third.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = ZzyThirdPartyApplication.class)
class ZzyThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Test
    public void testUploadFile() throws FileNotFoundException {
        try {
            //填写本地文件的完整路径
            InputStream inputStream = new FileInputStream("C:\\Users\\zzy\\Downloads\\iphone xr.jpg");
            // 依此填写Bucket名称和Object完整路径。Object完整路径不能包含Bucket
            ossClient.putObject("zhouziyang-mall", "iphone x.jpg", inputStream);
            System.out.println("上传成功...");
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void testSendSMS1(){
        smsComponent.sendSmsCode("16671131204","1204");
    }

    @Test
    public void testSendSMS() {
        String host = "https://wwsms.market.alicloudapi.com";
        String path = "/send_sms";
        String method = "POST";
        String appcode = "0de38643bd414257801a01c957337d7b";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:1204");
        bodys.put("template_id", "wangweisms996");   //注意，模板wangweisms996 仅作调试使用，下发短信不稳定，请联系客服报备自己的专属签名模板，以保障业务稳定使用
        bodys.put("phone_number", "16671131204");

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
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}