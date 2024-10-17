package com.zzy.mall.product;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzy.mall.product.entity.BrandEntity;
import com.zzy.mall.product.service.BrandService;
import com.zzy.mall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

// 指定启动类
@SpringBootTest(classes = ZzyProductApplication.class)
public class ZzyProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    public void test01() {
        BrandEntity entity = new BrandEntity();
        entity.setName("魅族");
        brandService.save(entity);
    }

    @Test
    public void selectAll() {
        List<BrandEntity> list = brandService.list();
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }

    @Test
    public void selectById() {
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq(" brand_id", 2));
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }

    @Autowired
    private OSSClient ossClient;

    @Test
    public void testUploadFile() throws FileNotFoundException {
        /*// Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-wuhan-lr.aliyuncs.com";

        String accessKeyId = "LTAI5tBZcnJqZn4zqDGtiwkG";
        String accessKeySecret = "zS2RNYlJpjs8tI0kQX3pkITZ3vwFx9";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);*/

        try {
            //填写本地文件的完整路径
            InputStream inputStream = new FileInputStream("C:\\Users\\zzy\\Downloads\\iphone xr.jpg");
            // 依此填写Bucket名称和Object完整路径。Object完整路径不能包含Bucket
            ossClient.putObject("zhouziyang-mall","iphone xrs.jpg",inputStream);
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
    CategoryService categoryService;

    @Test
    public void testPath(){
        Long[] catelogPath = categoryService.findCatelogPath(376L);
        System.out.println(Arrays.toString(catelogPath));
    }

}
