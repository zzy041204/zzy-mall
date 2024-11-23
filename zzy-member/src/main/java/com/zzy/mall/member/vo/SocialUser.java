package com.zzy.mall.member.vo;

import lombok.Data;

@Data
public class SocialUser {

    private String accessToken;

    private Long remindIn;

    private Long expiresIn;

    private String uid;

    private Boolean isRealName;

}
