package com.zzy.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:15:58
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

