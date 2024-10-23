package com.zzy.mall.coupon.service.impl;

import com.zzy.mall.common.dto.SkuReductionDTO;
import com.zzy.mall.coupon.entity.MemberPriceEntity;
import com.zzy.mall.coupon.entity.SkuLadderEntity;
import com.zzy.mall.coupon.service.MemberPriceService;
import com.zzy.mall.coupon.service.SkuLadderService;
import com.zzy.mall.coupon.service.SpuBoundsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.coupon.dao.SkuFullReductionDao;
import com.zzy.mall.coupon.entity.SkuFullReductionEntity;
import com.zzy.mall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionDTO dto) {
        // 1.折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(dto.getSkuId());
        skuLadderEntity.setFullCount(dto.getFullCount());
        skuLadderEntity.setDiscount(dto.getDiscount());
        skuLadderEntity.setAddOther(dto.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }
        // 2.满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(dto, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(dto.getPriceStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            this.save(skuFullReductionEntity);
        }
        // 3.会员价
        if (dto.getMemberPrice() != null && dto.getMemberPrice().size() > 0){
            List<MemberPriceEntity> memberPriceEntities = dto.getMemberPrice().stream().map(m -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(dto.getSkuId());
                memberPriceEntity.setMemberLevelId(m.getId());
                memberPriceEntity.setMemberLevelName(m.getName());
                memberPriceEntity.setMemberPrice(m.getPrice());
                memberPriceEntity.setAddOther(1); //是否可叠加
                return memberPriceEntity;
            }).collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntities);
        }
    }

}