package com.zzy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zzy.mall.product.entity.BrandEntity;
import com.zzy.mall.product.entity.CategoryEntity;
import com.zzy.mall.product.service.BrandService;
import com.zzy.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.CategoryBrandRelationDao;
import com.zzy.mall.product.entity.CategoryBrandRelationEntity;
import com.zzy.mall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        // 根据类别编号和品牌编号查询出对应的类别名称和品牌名称
        Long brandId = categoryBrandRelation.getBrandId();
        Long catId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryService.getById(catId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrandName(Long brandId, String name) {
        UpdateWrapper<CategoryBrandRelationEntity> wrapper = new UpdateWrapper<>();
        wrapper.set("brand_name", name).eq("brand_id", brandId);
        this.update(wrapper);
    }

    @Override
    public void updateCatelogName(Long catId, String name) {
        UpdateWrapper<CategoryBrandRelationEntity> wrapper = new UpdateWrapper<>();
        wrapper.set("catelog_name", name).eq("catelog_id", catId);
        this.update(wrapper);
    }

    /**
     * 根据类别编号查询所有的品牌信息
     *
     * @param catId
     * @return
     */
    @Override
    public List<CategoryBrandRelationEntity> categoryBrandRelation(Long catId) {
        List<CategoryBrandRelationEntity> list = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        return list;
    }

}