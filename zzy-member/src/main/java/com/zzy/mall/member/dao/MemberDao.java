package com.zzy.mall.member.dao;

import com.zzy.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:16:56
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
