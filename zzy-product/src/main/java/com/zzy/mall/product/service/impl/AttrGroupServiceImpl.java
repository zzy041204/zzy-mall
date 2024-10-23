package com.zzy.mall.product.service.impl;

import com.zzy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zzy.mall.product.entity.AttrEntity;
import com.zzy.mall.product.service.AttrAttrgroupRelationService;
import com.zzy.mall.product.service.AttrService;
import com.zzy.mall.product.vo.AttrGroupWithAttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.AttrGroupDao;
import com.zzy.mall.product.entity.AttrGroupEntity;
import com.zzy.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询列表数据
     * 根据类别编号来查询
     * @param params
     * @param catelogId 如果catelogId为0 只根据params来查询
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // 获取检索的关键字
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            //拼接查询的条件
            wrapper.and(obj ->{
               obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if (catelogId == 0){
            // 不根据catelogId来查询
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
        //根据类别编号来查询属性信息
        wrapper.eq("catelog_id", catelogId);
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrVO> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrVO> list = attrGroupEntityList.stream().map(attrGroupEntity -> {
            // 设置基本属性
            AttrGroupWithAttrVO attrGroupWithAttrVO = new AttrGroupWithAttrVO();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrVO);
            // 根据属性组id 查询属性组和基本属性关联信息 找到属性组中的所有基本属性id
            Long attrGroupId = attrGroupEntity.getAttrGroupId();
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupId);
            // 设置属性组中的基本属性信息
            attrGroupWithAttrVO.setAttrs(attrs);
            return attrGroupWithAttrVO;
        }).collect(Collectors.toList());
        return list;
    }

}