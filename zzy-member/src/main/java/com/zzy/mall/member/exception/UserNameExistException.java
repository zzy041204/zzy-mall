package com.zzy.mall.member.exception;

public class UserNameExistException extends RuntimeException{

    public UserNameExistException() {
        super("账号已存在");
    }

}
