package com.zzy.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zzy.mall.common.constant.OrderConstant;
import com.zzy.mall.common.exception.BizCodeEnume;
import com.zzy.mall.common.exception.NoStockException;
import com.zzy.mall.common.exception.RepeatSubmitException;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.common.vo.MemberVO;
import com.zzy.mall.order.dto.OrderCreateTO;
import com.zzy.mall.order.entity.OrderItemEntity;
import com.zzy.mall.order.feign.CartFeignService;
import com.zzy.mall.order.feign.MemberFeignService;
import com.zzy.mall.order.feign.ProductFeignService;
import com.zzy.mall.order.feign.WareFeignService;
import com.zzy.mall.order.interceptor.AuthInterceptor;
import com.zzy.mall.order.service.OrderItemService;
import com.zzy.mall.order.utils.OrderMsgProducer;
import com.zzy.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.order.dao.OrderDao;
import com.zzy.mall.order.entity.OrderEntity;
import com.zzy.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    OrderMsgProducer orderMsgProducer;


    private Lock lock = new ReentrantLock();


    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取订单确认页中需要获取的相关信息
     *
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVO confirmVO = new OrderConfirmVO();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.查询当前登录用户对应的会员的地址信息
            Long id = memberVO.getId();
            List<MemberAddressVO> address = memberFeignService.getAddress(id);
            if (address != null && address.size() > 0) {
                confirmVO.setAddress(address);
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.查询购物车中选择的商品信息
            List<OrderItemVO> items = cartFeignService.getUserCartItems();
            if (items != null && items.size() > 0) {
                confirmVO.setItems(items);
            }
        }, threadPoolExecutor);

        // 3.计算订单的总金额和需要支付的总金额 VO自动计算

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            // 4.生成防重的Token
            String token = UUID.randomUUID().toString().replace("-", "");
            // 把这个token信息存储到redis中 key= order:token:用户编号
            stringRedisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId(), token);
            confirmVO.setOrderToken(token);
        }, threadPoolExecutor);
        CompletableFuture.allOf(future1, future2, future3).get();
        return confirmVO;
    }

    /**
     *  Seata分布式事务管理 通过@GlobalTransactional修饰
     * @param vo
     * @return
     * @throws NoStockException
     * @throws RepeatSubmitException
     */
    //@GlobalTransactional
    @Transactional
    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockException,RepeatSubmitException {
        OrderResponseVO responseVO = new OrderResponseVO();
        // 获取当前登录的用户信息
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        lock.lock();
        try {
            // 验证是否重复提交
            String token = stringRedisTemplate.opsForValue().get(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId());
            if (token != null && token.equals(vo.getOrderToken())) {
                // 1.第一次提交 删除token
                stringRedisTemplate.delete(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId());
                // 2.创建订单
                OrderCreateTO orderCreateTO = createOrder(vo);
                // 3.保存订单信息
                saveOrder(orderCreateTO);
                // 4.锁定库存
                lockWare(orderCreateTO,responseVO);
                // 5.同步更新会员积分
                //int i = 1 / 0;
                // 订单成功保存后 需要给消息中间件发送延迟30分钟的关单消息
                orderMsgProducer.sendOrderMessage(orderCreateTO.getOrderEntity().getOrderSn());
            } else {
                // 表示重复提交
                responseVO.setCode(1);
                throw new RepeatSubmitException();
            }
            return responseVO;
        }finally {
            lock.unlock();
        }
    }

    private void lockWare(OrderCreateTO orderCreateTO,OrderResponseVO responseVO) throws NoStockException {
        // 4.锁定库存信息
        // 订单号 skuId skuName 商品的数量
        WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
        // 封装对象
        wareSkuLockVO.setOrderSn(orderCreateTO.getOrderEntity().getOrderSn());
        List<OrderItemVO> orderItemVOS = orderCreateTO.getOrderItemEntity().stream().map((item) -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            orderItemVO.setSkuId(item.getSkuId());
            orderItemVO.setTitle(item.getSkuName());
            orderItemVO.setCount(item.getSkuQuantity());
            return orderItemVO;
        }).collect(Collectors.toList());
        wareSkuLockVO.setOrderItems(orderItemVOS);
        R r = wareFeignService.orderLockStock(wareSkuLockVO);
        if (r.getCode() == 0) {
            // 锁定库存成功
            responseVO.setCode(0);
            responseVO.setOrderEntity(orderCreateTO.getOrderEntity());
        } else {
            responseVO.setCode(2); // 库存不足 锁定失败
            throw new NoStockException(10000l);
        }
    }

    /**
     * 保存订单数据到数据库
     *
     * @param orderCreateTO
     */
    private void saveOrder(OrderCreateTO orderCreateTO) {
        // 订单数据
        OrderEntity orderEntity = orderCreateTO.getOrderEntity();
        this.save(orderEntity);
        // 订单项数据
        List<OrderItemEntity> orderItemEntity = orderCreateTO.getOrderItemEntity();
        orderItemService.saveBatch(orderItemEntity);
    }

    /**
     * 创建订单的方法
     *
     * @param vo
     * @return
     */
    private OrderCreateTO createOrder(OrderSubmitVO vo) {
        OrderCreateTO createTO = new OrderCreateTO();
        // 创建OrderEntity
        OrderEntity orderEntity = createOrderEntity(vo);
        // 创建OrderItemEntity
        List<OrderItemEntity> orderItemEntities = createOrderItemEntity(orderEntity.getOrderSn());
        createTO.setOrderEntity(orderEntity);
        createTO.setOrderItemEntity(orderItemEntities);
        return createTO;
    }

    /**
     * 通过购物车中选中的商品 来创建对应的购物项
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> createOrderItemEntity(String orderSn) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        // 获取购物车中选中的商品信息
        List<OrderItemVO> items = cartFeignService.getUserCartItems();
        if (items != null && items.size() > 0) {
            // 统一根据spuId 查询对应的spu的信息
            List<Long> spuIds = new ArrayList<>();
            for (OrderItemVO item : items) {
                if (!spuIds.contains(item.getSpuId())) {
                    // 不包含的spu 则添加到list中
                    spuIds.add(item.getSpuId());
                }
            }
            // 远程调用商品服务获取对应的spu信息
            List<OrderItemSpuInfoVO> spuInfos = productFeignService.getOrderItemSpuInfoBySpuId(spuIds.toArray(new Long[spuIds.size()]));
            Map<Long, OrderItemSpuInfoVO> map = spuInfos.stream().collect(Collectors.toMap(OrderItemSpuInfoVO::getId, item -> item));
            for (OrderItemVO item : items) {
                // 获取商品对应的spu信息
                OrderItemSpuInfoVO spuInfo = map.get(item.getSpuId());
                OrderItemEntity orderItemEntity = buildOrderItem(item, spuInfo);
                // 绑定订单编号
                orderItemEntity.setOrderSn(orderSn);
                orderItemEntities.add(orderItemEntity);
            }
        }
        return orderItemEntities;
    }

    /**
     * 根据购物车的一个商品 创建一个订单项
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVO item, OrderItemSpuInfoVO spuInfo) {
        OrderItemEntity entity = new OrderItemEntity();
        // sku信息
        entity.setSkuId(item.getSkuId());
        entity.setSkuName(item.getTitle());
        entity.setSkuPic(item.getImage());
        entity.setSkuPrice(item.getPrice());
        entity.setSkuQuantity(item.getCount());
        List<String> skuAttr = item.getSkuAttr();
        String skuAttrStr = StringUtils.collectionToDelimitedString(skuAttr, ";");
        entity.setSkuAttrsVals(skuAttrStr);
        // spu信息
        entity.setSpuId(spuInfo.getId());
        entity.setSpuName(spuInfo.getSpuName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setSpuBrand(spuInfo.getBrandName());
        entity.setSpuPic(spuInfo.getImg());
        // 优惠信息
        // 积分信息
        entity.setGiftGrowth(item.getPrice().intValue());
        entity.setGiftIntegration(item.getPrice().intValue());
        return entity;
    }

    /**
     * 创建OrderEntity
     *
     * @param vo
     * @return
     */
    private OrderEntity createOrderEntity(OrderSubmitVO vo) {
        OrderEntity orderEntity = new OrderEntity();
        // 创建订单编号
        String timeId = IdWorker.getTimeId();
        orderEntity.setOrderSn(timeId); // 设置订单编号
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 设置会员相关的信息
        orderEntity.setMemberId(memberVO.getId());
        orderEntity.setMemberUsername(memberVO.getNickname());
        // 根据收获地址ID 获取收货地址的详情信息
        MemberAddressVO memberAddressVO = memberFeignService.getAddressById(vo.getAddrId());
        orderEntity.setReceiverCity(memberAddressVO.getCity());
        orderEntity.setReceiverDetailAddress(memberAddressVO.getDetailAddress());
        orderEntity.setReceiverName(memberAddressVO.getName());
        orderEntity.setReceiverPhone(memberAddressVO.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVO.getPostCode());
        orderEntity.setReceiverRegion(memberAddressVO.getRegion());
        orderEntity.setReceiverProvince(memberAddressVO.getProvince());
        // 设置订单状态
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.WAIT_FOR_PAYMENT.getCode());
        return orderEntity;
    }

}