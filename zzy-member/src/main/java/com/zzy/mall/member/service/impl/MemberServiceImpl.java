package com.zzy.mall.member.service.impl;

import com.zzy.mall.member.entity.MemberLevelEntity;
import com.zzy.mall.member.exception.PhoneExistException;
import com.zzy.mall.member.exception.UserNameExistException;
import com.zzy.mall.member.service.MemberLevelService;
import com.zzy.mall.member.vo.MemberLoginVO;
import com.zzy.mall.member.vo.MemberRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.member.dao.MemberDao;
import com.zzy.mall.member.entity.MemberEntity;
import com.zzy.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 完成会员的注册功能
     * @param vo
     */
    @Override
    public void register(MemberRegisterVO vo) throws PhoneExistException, UserNameExistException {
        MemberEntity memberEntity = new MemberEntity();
        // 设置会员等级 默认值
        MemberLevelEntity memberLevelEntity = memberLevelService.queryMemberLevelDefault();
        memberEntity.setLevelId(memberLevelEntity.getId());
        // 添加对应的账号和手机号不能重复
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        // 需要对密码进行加密处理
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        // 设置其他的默认值
        memberEntity.setCreateTime(new Date());
        this.save(memberEntity);
    }

    @Override
    public MemberEntity login(MemberLoginVO vo) {
        // 1.根据账号或者手机号来查询会员信息
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", vo.getUserName())
                .or()
                .eq("mobile", vo.getUserName()));
        if (memberEntity != null) {
            // 2.如果账号或手机号存在 然后根据加密规则来校验 判断是否登录成功
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(vo.getPassword(), memberEntity.getPassword());
            if (matches) {
                // 表明登录成功
                return memberEntity;
            }
        }
        return null;
    }

    /**
     * 校验账号是否存在
     * @param userName
     * @throws UserNameExistException
     */
    private void checkUserNameUnique(String userName) throws UserNameExistException {
        long count = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            // 说明账号存在
            throw new UserNameExistException();
        }
    }

    /**
     * 校验手机号是否存在
     * @param phone
     * @throws PhoneExistException
     */
    private void checkPhoneUnique(String phone) throws PhoneExistException {
        long count = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            // 说明手机号存在
            throw new PhoneExistException();
        }
    }

}