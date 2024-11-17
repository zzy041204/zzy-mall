package com.zzy.mall.auth.utils;

import java.util.Random;

public class RandomCodeUtils {

    public static String getRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(10);  // 生成 0 到 9 之间的随机数
            sb.append(digit);
        }
        String randomNumberString = sb.toString();
        return randomNumberString;
    }

}
