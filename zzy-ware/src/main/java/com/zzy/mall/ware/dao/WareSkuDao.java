package com.zzy.mall.ware.dao;

import com.zzy.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:18:29
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<Long> listHasStock(Long skuId);
}
