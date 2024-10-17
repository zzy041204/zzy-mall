package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.BrandEntity;
import com.zzy.mall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrandName(Long brandId, String name);

    void updateCatelogName(Long catId, String name);

    List<BrandEntity> categoryBrandRelation(Long catId);
}

