package com.zzy.mall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.constant.OrderConstant;
import com.zzy.mall.common.constant.SeckillConstant;
import com.zzy.mall.common.dto.SeckillOrderDTO;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.common.vo.MemberVO;
import com.zzy.mall.seckill.dto.SeckillSkuRedisDTO;
import com.zzy.mall.seckill.feign.CouponFeignService;
import com.zzy.mall.seckill.feign.ProductFeignService;
import com.zzy.mall.seckill.interceptor.AuthInterceptor;
import com.zzy.mall.seckill.service.SeckillService;
import com.zzy.mall.seckill.vo.SeckillSessionEntity;
import com.zzy.mall.seckill.vo.SkuInfoVO;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void uploadSeckillSku3Days() {
        // 1.通过OpenFeign远程调用Coupon服务中的接口来获取未来三天的秒杀活动的商品
        R r = couponFeignService.getLater3DaysSession();
        if (r.getCode() == 0) {
            // 1.表示查询操作成功
             String json = (String) r.get("data");
             List<SeckillSessionEntity> seckillSessionEntities = JSON.parseArray(json, SeckillSessionEntity.class);
             // 2.上架商品 将数据保存到Redis中
            // 缓存商品
            // 2.1 缓存每日秒杀的基本信息
            saveSessionInfos(seckillSessionEntities);
            // 2.2 缓存SKU的基本信息
            saveSessionSKUInfos(seckillSessionEntities);
        }
    }

    public List<SeckillSkuRedisDTO> blockHandlerGetCurrentSeckillSkus(BlockException ex){
        log.error("限流执行的blockHandler{}",ex.getMessage());
        return null;
    }

    /**
     * 获取当前时间段的秒杀活动及对应的商品SKU信息
     * @return
     */
    @SentinelResource(value = "getCurrentSeckillSkusResources",blockHandler = "blockHandlerGetCurrentSeckillSkus")
    @Override
    public List<SeckillSkuRedisDTO> getCurrentSeckillSkus() {
        try (Entry entry = SphU.entry("getCurrentSeckillSkus")){
            // 1.确定当前时间属于哪个秒杀活动的
            long time = new Date().getTime();
            // 从redis中查询所有的秒杀活动
            Set<String> keys = stringRedisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");
            for (String key : keys) {
                String replace = key.replace(SeckillConstant.SESSION_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                Long start = Long.parseLong(s[0]);
                Long end = Long.parseLong(s[1]);
                if (time > start && time < end) {
                    // 当前的秒杀活动就是当前时间需要参与的活动
                    List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                    // 取出来的是skuId 1_6
                    BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
                    List<SeckillSkuRedisDTO> seckillSkuRedisDTOS = range.stream().map(item -> {
                        String json = ops.get(item);
                        SeckillSkuRedisDTO seckillSkuRedisDTO = JSON.parseObject(json, SeckillSkuRedisDTO.class);
                        return seckillSkuRedisDTO;
                    }).collect(Collectors.toList());
                    return seckillSkuRedisDTOS;
                }
            }
            return null;
        }catch (BlockException ex){
            log.error("getCurrentSeckillSkus被限制访问了...");
            return null;
        }
    }

    @Override
    public SeckillSkuRedisDTO getSeckillSessionBySkuId(Long skuId) {
        // 1.找到所有需要秒杀商品的sku信息
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                boolean matches = Pattern.matches(regx, key);
                if (matches) {
                    String json = ops.get(key);
                    SeckillSkuRedisDTO dto = JSON.parseObject(json, SeckillSkuRedisDTO.class);
                    return dto;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer num) {
        // 1.根据skillId取到当前秒杀的商品信息
        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
        String json = hashOperations.get(killId);
        if (StringUtils.isNotBlank(json)){
            SeckillSkuRedisDTO dto = JSON.parseObject(json, SeckillSkuRedisDTO.class);
            // 校验时效性
            Long startTime = dto.getStartTime();
            Long endTime = dto.getEndTime();
            long now = new Date().getTime();
            if (now > startTime && now < endTime) {
                // 说明请求在秒杀时间段中
                // 2.校验 随机码和商品是否合法
                String randomCode = dto.getRandomCode();
                String redisKillId = dto.getPromotionSessionId() + "_" + dto.getSkuId();
                if (randomCode.equals(code) && redisKillId.equals(killId)) {
                    // 随机码校验合法
                    // 3. 判断抢购商品数量是否合法
                    if (num <= dto.getSeckillLimit().intValue()){
                        // 满足限购的条件
                        // 4.判断是否满足幂等性
                        // 只要抢购成功就在redis存储一条信息 userId + sessionId + skuId
                        MemberVO memberVO = AuthInterceptor.threadLocal.get();
                        Long userId = memberVO.getId();
                        String redisKey = userId + "_" + redisKillId;
                        Boolean setIfAbsent = stringRedisTemplate.opsForValue()
                                .setIfAbsent(redisKey, num.toString(), (endTime - now), TimeUnit.MILLISECONDS);
                        if (setIfAbsent){
                            // 表示数据插入成功 是第一次操作
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b){
                                    // 获取信号量信息 秒杀成功
                                    String orderSn = UUID.randomUUID().toString().replace("-", "");
                                    // TODO 继续完成下订单的操作 ---> RocketMQ
                                    SeckillOrderDTO seckillOrderDTO = new SeckillOrderDTO();
                                    seckillOrderDTO.setOrderSn(orderSn);
                                    seckillOrderDTO.setMemberId(userId);
                                    seckillOrderDTO.setSeckillPrice(dto.getSeckillPrice());
                                    seckillOrderDTO.setNum(num);
                                    seckillOrderDTO.setPromotionSessionId(dto.getPromotionSessionId().toString());
                                    seckillOrderDTO.setSkuId(dto.getSkuId().toString());
                                    // 通过RocketMQ 发送异步消息
                                    rocketMQTemplate.sendOneWay(OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC,
                                            JSON.toJSONString(seckillOrderDTO));
                                    return orderSn;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 存储秒杀活动到对应的SKU信息
     * @param seckillSessionEntities
     */
    private void saveSessionSKUInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        seckillSessionEntities.stream().forEach(seckillSessionEntity -> {
            // 循环取出每个活动的Session 然后取出对应的SkuID 封装相关的信息
            BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
            seckillSessionEntity.getRelationEntities().stream().forEach(item -> {
                String key = item.getPromotionSessionId() + "_" + item.getSkuId();
                if (!hashOperations.hasKey(key)) {
                    SeckillSkuRedisDTO dto = new SeckillSkuRedisDTO();
                    // 1.获取SKU的基本信息
                    R r = productFeignService.info(item.getSkuId());
                    if (r.getCode() == 0) {
                        String json = (String) r.get("skuInfoJSON");
                        SkuInfoVO skuInfoVO = JSON.parseObject(json, SkuInfoVO.class);
                        dto.setSkuInfoVO(skuInfoVO);
                    }
                    // 2.获取SKU的秒杀信息
                    /*dto.setSkuId(item.getSkuId());
                     dto.setSeckillPrice(item.getSeckillPrice());
                     dto.setSeckillCount(item.getSeckillCount());
                     dto.setSeckillLimit(item.getSeckillLimit());
                     dto.setSeckillSort(item.getSeckillSort());*/
                    BeanUtils.copyProperties(item, dto);
                    // 3.设置当前商品的秒杀时间
                    dto.setStartTime(seckillSessionEntity.getStartTime().getTime());
                    dto.setEndTime(seckillSessionEntity.getEndTime().getTime());
                    // 4.随机码
                    String token = UUID.randomUUID().toString().replace("-","");
                    dto.setRandomCode(token);
                    // 5.绑定对应的活动编号
                    dto.setPromotionSessionId(item.getPromotionSessionId());
                    // 6.分布式信号量的处理 请求限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    // 商品数量作为信号量
                    semaphore.trySetPermits(item.getSeckillCount().intValue());
                    hashOperations.put(key,JSON.toJSONString(dto));
                    hashOperations.expireAt(seckillSessionEntity.getEndTime());
                }
            });
        });
    }

    /**
     * 保存每日秒杀活动的信息到Redis中
     * @param seckillSessionEntities
     */
    private void saveSessionInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        for (SeckillSessionEntity seckillSessionEntity : seckillSessionEntities) {
            // 缓存每一个秒杀活动
            long start = seckillSessionEntity.getCreateTime().getTime();
            long end = seckillSessionEntity.getEndTime().getTime();
            // 生成key
            String key = SeckillConstant.SESSION_CACHE_PREFIX + start + "_" + end;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey){ // 说明redis里面没有缓存这个秒杀活动 需要保存到redis中
                List<String> collect = seckillSessionEntity.getRelationEntities().stream().map(seckillSkuRelationEntity -> {
                    return seckillSkuRelationEntity.getPromotionSessionId() + "_" + seckillSkuRelationEntity.getSkuId().toString();
                }).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
                stringRedisTemplate.expireAt(key, seckillSessionEntity.getEndTime());
            }
        }
    }
}
