package com.zzy.mall.common.constant;

/**
 * 订单模块涉及到的常量
 */
public class OrderConstant {

    public static final String ORDER_TOKEN_PREFIX = "order:token";

    public enum OrderStatusEnum {

        WAIT_FOR_PAYMENT(0,"待付款"),
        WAIT_SEND_GOOD(1,"待发货"),
        HAS_SENT(2,"已发货"),
        HAS_COMPLETED(3,"已完成"),
        HAS_CLOSED(4,"已关闭"),
        INVALID_ORDER(5,"无效订单");

        private int code;
        private String msg;

        OrderStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode(){
            return code;
        }

        public String getMsg(){
            return msg;
        }
    }

}
