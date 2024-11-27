package com.zzy.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zzy.mall.common.dto.SkuStockDTO;
import com.zzy.mall.common.exception.NoStockException;
import com.zzy.mall.ware.feign.ProductFeignService;
import com.zzy.mall.ware.vo.OrderItemVO;
import com.zzy.mall.ware.vo.SkuWareReduceResultVo;
import com.zzy.mall.ware.vo.WareSkuLockVO;
import com.zzy.mall.ware.vo.WareSkuReduceVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.ware.dao.WareSkuDao;
import com.zzy.mall.ware.entity.WareSkuEntity;
import com.zzy.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper.eq(wareId != null && wareId != "","ware_id", wareId)
                        .eq(skuId != null && skuId != "","sku_id", skuId)
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity wareSkuEntity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId).eq("ware_id", wareId));
        if (wareSkuEntity != null){
            // 在已有的库存信息里面添加库存数量
            Integer stock = wareSkuEntity.getStock();
            wareSkuEntity.setStock( stock + skuNum);
            this.updateById(wareSkuEntity);
        }else {
            // 新增一条库存信息
            WareSkuEntity skuEntity = new WareSkuEntity();
            String skuName = productFeignService.infoNameById(skuId);
            skuEntity.setSkuId(skuId);
            skuEntity.setWareId(wareId);
            skuEntity.setStock(skuNum);
            skuEntity.setSkuName(skuName);
            skuEntity.setStockLocked(0);
            this.save(skuEntity);
        }
    }

    @Override
    public List<SkuStockDTO> HasStock(List<Long> skuIds) {
        List<SkuStockDTO> list = skuIds.stream().map(skuId -> {
            List<WareSkuEntity> wareSkuEntityList = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));
            Long sum = 0l;
            for (WareSkuEntity wareSkuEntity : wareSkuEntityList) {
                sum += (wareSkuEntity.getStock() - wareSkuEntity.getStockLocked());
            }
            SkuStockDTO skuStockDTO = new SkuStockDTO();
            if (sum > 0) {
                skuStockDTO.setSkuId(skuId);
                skuStockDTO.setHasStock(true);
            } else {
                skuStockDTO.setSkuId(skuId);
                skuStockDTO.setHasStock(false);
            }
            return skuStockDTO;
        }).collect(Collectors.toList());
        return list;
    }

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        List<OrderItemVO> orderItems = vo.getOrderItems();
        // 首先找到具有库存的仓库
        List<SkuWareHasStock> stockList = orderItems.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            List<Long> wareIds = this.baseMapper.listHasStock(item.getSkuId());
            skuWareHasStock.setSkuId(item.getSkuId());
            skuWareHasStock.setNum(item.getCount());
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock skuWareHasStock : stockList) {
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                // 表示当前商品没有库存了
                throw new NoStockException(skuId);
            }
            int count = skuWareHasStock.getNum();
            for (Long wareId : wareIds) {
                // 循环到对应的仓库然后锁定库存
                WareSkuEntity wareSkuEntity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
                int canStock = wareSkuEntity.getStock() - wareSkuEntity.getStockLocked();
                if (canStock >= count) {
                    this.update(new UpdateWrapper<WareSkuEntity>().set("stock_locked",wareSkuEntity.getStockLocked()+count).eq("sku_id", skuId).eq("ware_id", wareId));
                    count = 0;
                    break;
                }else {
                    this.update(new UpdateWrapper<WareSkuEntity>().set("stock_locked",wareSkuEntity.getStockLocked()+canStock).eq("sku_id", skuId).eq("ware_id", wareId));
                    count -= canStock;
                }
            }
            if( count > 0){
                // 表示商品没有锁定成功
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Transactional
    @Override
    public Boolean reduceStock(List<WareSkuReduceVO> list) {
        List<SkuWareReduceResultVo> resultList = list.stream().map(reduceVO -> {
            SkuWareReduceResultVo skuWareReduceResultVo = new SkuWareReduceResultVo();
            skuWareReduceResultVo.setSkuId(reduceVO.getSkuId());
            List<WareSkuEntity> wareSkuEntityList = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", reduceVO.getSkuId()));
            Integer skuQuantity = reduceVO.getSkuQuantity();
            for (WareSkuEntity skuEntity : wareSkuEntityList) {
                if (skuEntity.getStockLocked() >= skuQuantity) {
                    this.update(new UpdateWrapper<WareSkuEntity>().set("stock", skuEntity.getStock() - skuQuantity).set("stock_locked", skuEntity.getStockLocked() - skuQuantity).eq("sku_id", reduceVO.getSkuId()).eq("ware_id", skuEntity.getWareId()));
                    skuQuantity = 0;
                    break;
                } else {
                    skuQuantity -= skuEntity.getStockLocked();
                    this.update(new UpdateWrapper<WareSkuEntity>().set("stock", skuEntity.getStock() - skuEntity.getStockLocked()).set("stock_locked", 0).eq("sku_id", reduceVO.getSkuId()).eq("ware_id", skuEntity.getWareId()));
                }
            }
            if (skuQuantity > 0) {
                skuWareReduceResultVo.setResult(false);
            } else {
                skuWareReduceResultVo.setResult(true);
            }
            return skuWareReduceResultVo;
        }).collect(Collectors.toList());
        for (SkuWareReduceResultVo skuWareReduceResultVo : resultList) {
            if (skuWareReduceResultVo.getResult() == false){
                return false;
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}