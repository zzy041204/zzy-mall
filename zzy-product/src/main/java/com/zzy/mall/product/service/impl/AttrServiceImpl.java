package com.zzy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.mall.common.constant.ProductConstant;
import com.zzy.mall.product.dao.AttrAttrgroupRelationDao;
import com.zzy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zzy.mall.product.entity.AttrGroupEntity;
import com.zzy.mall.product.entity.CategoryEntity;
import com.zzy.mall.product.service.*;
import com.zzy.mall.product.vo.AttrGroupRelationVO;
import com.zzy.mall.product.vo.AttrResponseVO;
import com.zzy.mall.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
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

import com.zzy.mall.product.dao.AttrDao;
import com.zzy.mall.product.entity.AttrEntity;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVO vo) {
        // 1.保存规格参数的正常信息
        AttrEntity attrEntity = new AttrEntity();
        // 拷贝源对象属性到目标对象(浅拷贝 新增指针指向vo的字段)
        BeanUtils.copyProperties(vo, attrEntity);
        this.save(attrEntity);
        if (vo.getAttrGroupId() != null && vo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 2.保存规格参数和属性组对应的信息
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            // attrEntity.getAttrId() 主键值是由数据库创建的 新增属性时vo.getAttrId()为空 要使用attrEntity.getAttrId()
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationService.save(relationEntity);
        }
    }

    @Override
    public PageUtils queryBasePage(Map<String, Object> params, Long categlogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? 1 : 0);
        // 1.根据类别编号查询
        if (categlogId != 0) {
            wrapper.eq("catelog_id", categlogId);
        }
        // 2.根据key模糊查询
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(o -> {
                o.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // 3.分页查询
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        // 4.关联类别名称和分组名称
        List<AttrResponseVO> list = new ArrayList<>();
        page.getRecords().forEach(attrEntity -> {
            AttrResponseVO vo = new AttrResponseVO();
            BeanUtils.copyProperties(attrEntity, vo);
            // 设置类别名称
            Long catelogId = attrEntity.getCatelogId();
            CategoryEntity categoryEntity = categoryService.getById(catelogId);
            if (categoryEntity != null) {
                vo.setCatelogName(categoryEntity.getName());
            }
            if ("base".equalsIgnoreCase(attrType)) {
                // 设置属性组名称
                Long attrId = attrEntity.getAttrId();
                AttrAttrgroupRelationEntity attrgroupRelationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                if (attrgroupRelationEntity != null && attrgroupRelationEntity.getAttrGroupId() != null) {
                    Long groupId = attrgroupRelationEntity.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(groupId);
                    if (attrGroupEntity != null) {
                        vo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }
            list.add(vo);
        });
        long total = page.getTotal();
        long size = page.getSize();
        long current = page.getCurrent();
        return new PageUtils(list, (int) total, (int) size, (int) current);
    }

    /**
     * 根据规格参数ID查询对应的详细信息
     * 1.规格参数的具体信息
     * 2.关联的属性组信息
     * 3.关联的类别信息
     *
     * @param attrId
     * @return
     */
    @Override
    public AttrResponseVO getAttrInfo(Long attrId) {
        AttrResponseVO vo = new AttrResponseVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, vo);
        // 返回类别信息
        Long catelogId = attrEntity.getCatelogId();
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (categoryEntity != null) {
            vo.setCatelogName(categoryEntity.getName());
        }
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        vo.setCatelogPath(catelogPath);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 返回属性组信息
            AttrAttrgroupRelationEntity attrgroupRelationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrgroupRelationEntity != null && attrgroupRelationEntity.getAttrGroupId() != null) {
                Long attrGroupId = attrgroupRelationEntity.getAttrGroupId();
                vo.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                if (attrGroupEntity != null) {
                    vo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        return vo;
    }

    @Transactional
    @Override
    public void updateBaseAttr(AttrVO attr) {
        // 1.更新基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 2.修改分组关联的关系
            Long attrId = attr.getAttrId();
            Long attrGroupId = attr.getAttrGroupId();
            long count = attrAttrgroupRelationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (count > 0) {
                // 有数据直接更新
                UpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("attr_group_id", attrGroupId).eq("attr_id", attrId);
                attrAttrgroupRelationService.update(updateWrapper);
            } else {
                // 没有数据直接插入
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                attrAttrgroupRelationEntity.setAttrId(attrId);
                attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
                attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
            }
        }
    }

    @Transactional
    @Override
    public void removeByIdsDetails(List<Long> list) {
        list.forEach(attrId -> {
            AttrEntity attrEntity = this.getById(attrId);
            if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                // 1.删除关联表中的数据
                attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            }
            // 2.删除属性表中的数据
            this.removeById(attrId);
        });
    }

    /**
     * 根据属性组编号查询对应的分组信息
     *
     * @param attrGroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        // 1.根据属性组 从属性组和基本信息关联表中查询对应的属性信息
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<AttrEntity> attrEntities = new ArrayList<>();
        for (AttrAttrgroupRelationEntity attrAttrgroupRelationEntity : attrAttrgroupRelationEntities) {
            // 2.根据属性id数组获取对应的信息
            Long attrId = attrAttrgroupRelationEntity.getAttrId();
            AttrEntity attrEntity = this.getById(attrId);
            if (attrEntity != null) {
                attrEntities.add(attrEntity);
            }
        }
        return attrEntities;
    }

    /**
     * 解除属性组和基本属性的关联关系
     * 删除属性组和基本属性关联表中的数据
     *
     * @param vos
     */
    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = new ArrayList<>();
        for (AttrGroupRelationVO vo : vos) {
            AttrAttrgroupRelationEntity attrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, attrgroupRelationEntity);
            attrAttrgroupRelationEntities.add(attrgroupRelationEntity);
        }
        attrAttrgroupRelationDao.removeBatchRelation(attrAttrgroupRelationEntities);
    }

    /**
     * 根据属性组ID查询出未被关联的属性信息
     * 1.查询出和属性组在同一类的所有的基本属性信息
     * 2.查询出同一类下所有属性组已关联的基本属性信息
     * 3.排除所有已关联的基本属性信息
     *
     * @param params
     * @param attrGroupId
     * @return
     */
    @Override
    public PageUtils getNoAttrRelation(Map<String, Object> params, Long attrGroupId) {
        // 1.查询当前属性组所在的类别编号
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2.查询当前类的所有属性组id
        List<AttrGroupEntity> group = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> groupIds = group.stream().map(g -> g.getAttrGroupId()).collect(Collectors.toList());
        // 3.查询当前类所有属性组关联的属性id
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = relationEntities.stream().map(r -> r.getAttrId()).collect(Collectors.toList());
        // 4.查询当前类下所有基本信息
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()).eq("catelog_id", catelogId);
        // 5.排除所有已关联属性组的基本属性信息
        if (attrIds.size() > 0 && attrIds != null) {
            wrapper.notIn("attr_id", attrIds);
        }
        // 6.根据key的查询操作
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            wrapper.and(attrEntity -> {
                attrEntity.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }


}