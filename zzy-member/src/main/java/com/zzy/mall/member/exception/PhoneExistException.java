package com.zzy.mall.member.exception;

/**
 * 手机号存在的自定义异常
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号存在");
    }

}
