package com.zzy.mall.coupon.service.impl;

import com.zzy.mall.coupon.entity.SeckillSkuRelationEntity;
import com.zzy.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.coupon.dao.SeckillSessionDao;
import com.zzy.mall.coupon.entity.SeckillSessionEntity;
import com.zzy.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3Days() {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.MIN);
        String begin = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3).with(LocalTime.MAX);
        String end = threeDaysLater.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", begin, end)
                .le("end_time", end));
        List<SeckillSessionEntity> sessionEntities = list.stream().map(seckillSessionEntity -> {
            // 根据sessionId 查询对应的活动商品信息
            Long id = seckillSessionEntity.getId();
            List<SeckillSkuRelationEntity> seckillSkuRelationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
            seckillSessionEntity.setRelationEntities(seckillSkuRelationEntities);
            return seckillSessionEntity;
        }).collect(Collectors.toList());
        return sessionEntities;
    }

}