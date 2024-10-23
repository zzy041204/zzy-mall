package com.zzy.mall.third;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
            ossClient.putObject("zhouziyang-mall","iphone x.jpg",inputStream);
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

}
