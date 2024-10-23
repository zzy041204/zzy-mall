package com.zzy.mall.order.dao;

import com.zzy.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:15:58
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
