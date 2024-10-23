package com.zzy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zzy.mall.product.entity.CategoryBrandRelationEntity;
import com.zzy.mall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.BrandDao;
import com.zzy.mall.product.entity.BrandEntity;
import com.zzy.mall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        // 1.获取key
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(key)){
            wrapper.eq("brand_id",key).or().like("name",key);;
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // 1.更新原始数据
        this.updateById(brand);
        if (StringUtils.isNotBlank(brand.getName())){
            // 同步更新级联关系
            Long brandId = brand.getBrandId();
            String name = brand.getName();
            categoryBrandRelationService.updateBrandName(brandId,name);
            // 同步更新其他冗余的数据
        }
    }

}