package com.zzy.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@SpringBootTest
class ZzyMemberApplicationTests {

    @Test
    void contextLoads() {
        // e10adc3949ba59abbe56e057f20f883e
        // e10adc3949ba59abbe56e057f20f883e
        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);
        // 加盐处理
        String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$zzy@1204#");
        System.out.println(s1);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String s2 = encoder.encode("123456");
        String s3 = encoder.encode("123456");
        System.out.println(s2);
        System.out.println(s3);
        boolean matches1 = encoder.matches("123456", s2);
        boolean matches2 = encoder.matches("123456", s3);
        System.out.println("matches1="+matches1);
        System.out.println("matches2="+matches2);
    }

}
