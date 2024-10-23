package com.zzy.mall.product.dao;

import com.zzy.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
