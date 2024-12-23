package com.zzy.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.member.entity.MemberEntity;
import com.zzy.mall.member.vo.MemberLoginVO;
import com.zzy.mall.member.vo.MemberRegisterVO;
import com.zzy.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:16:56
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVO vo);

    MemberEntity login(MemberLoginVO vo);

    MemberEntity socialLogin(SocialUser vo);
}

