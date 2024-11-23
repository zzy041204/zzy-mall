package com.zzy.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.exception.BizCodeEnume;
import com.zzy.mall.member.exception.PhoneExistException;
import com.zzy.mall.member.exception.UserNameExistException;
import com.zzy.mall.member.vo.MemberLoginVO;
import com.zzy.mall.member.vo.MemberRegisterVO;
import com.zzy.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzy.mall.member.entity.MemberEntity;
import com.zzy.mall.member.service.MemberService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.R;


/**
 * 会员
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:16:56
 */
@RestController
@RequestMapping("/member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 会员注册
     *
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVO vo) {
        try {
            memberService.register(vo);
        } catch (UserNameExistException exception) {
            return R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(), BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException exception) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (Exception exception) {
            return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(), BizCodeEnume.UNKNOWN_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVO vo) {
        MemberEntity member = memberService.login(vo);
        if (member != null) {
            return R.ok().put("entity", JSON.toJSONString(member));
        } else {
            return R.error(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getCode(),
                    BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/oauth2/login")
    public R socialLogin(@RequestBody SocialUser vo) {
        MemberEntity memberEntity = memberService.socialLogin(vo);
        String jsonString = JSON.toJSONString(memberEntity);
        return R.ok().put("entity", jsonString);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
