package com.zzy.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.exception.NoStockException;
import com.zzy.mall.common.exception.RepeatSubmitException;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.order.entity.OrderEntity;
import com.zzy.mall.order.vo.OrderConfirmVO;
import com.zzy.mall.order.vo.OrderResponseVO;
import com.zzy.mall.order.vo.OrderSubmitVO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:15:58
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockException, RepeatSubmitException;
}

