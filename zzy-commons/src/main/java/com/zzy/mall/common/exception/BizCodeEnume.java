package com.zzy.mall.common.exception;

/**
 * 错误编码和错误信息的枚举类
 * 10：通用的
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15:会员
 */
public enum BizCodeEnume {

    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式异常"),
    VALID_SMS_EXCEPTION(10002,"短信发送间隔不足一分钟，稍等片刻发送"),
    SEND_SMS_EXCEPTION(10002,"短信发送失败，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11001,"商品上架异常"),
    USERNAME_EXIST_EXCEPTION(15001,"用户名存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    USERNAME_PHONE_VALID_EXCEPTION(15003,"账号或者密码错误");


    private int code;
    private String msg;

    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
