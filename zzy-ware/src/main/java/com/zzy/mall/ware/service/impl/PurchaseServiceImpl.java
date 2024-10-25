package com.zzy.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zzy.mall.common.constant.WareConstant;
import com.zzy.mall.ware.entity.PurchaseDetailEntity;
import com.zzy.mall.ware.entity.WareSkuEntity;
import com.zzy.mall.ware.feign.ProductFeignService;
import com.zzy.mall.ware.service.PurchaseDetailService;
import com.zzy.mall.ware.service.WareSkuService;
import com.zzy.mall.ware.vo.MergeVO;
import com.zzy.mall.ware.vo.PurchaseDoneVO;
import com.zzy.mall.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.ware.dao.PurchaseDao;
import com.zzy.mall.ware.entity.PurchaseEntity;
import com.zzy.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询状态为新建或已分配的采购单
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageUnReceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode()).or().eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
               wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 完成采购需求的合单操作
     * @param mergeVO
     * @return
     */
    @Override
    public Integer merge(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId == null) {
            // 新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // 判断采购单的状态 只能是新建或者已分配才能合单
        PurchaseEntity purchase = this.getById(purchaseId);
        if (purchase != null && (purchase.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || purchase.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())) {
            final Long finalPurchaseId = purchaseId;
            List<Long> items = mergeVO.getItems();
            items.stream().forEach( i -> {
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
                if (purchaseDetailEntity != null && (purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode())) {
                    // 采购项状态为新建或已分配才能合单
                    purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                    purchaseDetailService.updateById(purchaseDetailEntity);
                    //更新采购单时间
                    this.update(new UpdateWrapper<PurchaseEntity>().set("update_time", new Date()).eq("id",finalPurchaseId));
                }
            });
            return 1;
        }
        return -1;
    }

    @Override
    public void received(List<Long> ids) {
        ids.stream().forEach( id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            // 1.领取采购单的状态只能是新建或者已分配采购单 其他的是不能领取的
            if ( purchaseEntity != null && (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() )) {
                // 2.更新采购单的状态为已领取
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                purchaseEntity.setUpdateTime(new Date());
                this.updateById(purchaseEntity);
                // 3.更新采购项的状态为正在采购
                purchaseDetailService.update(new UpdateWrapper<PurchaseDetailEntity>().set("status",WareConstant.PurchaseDetailStatusEnum.BUYING.getCode()).eq("purchase_id", id));
            }
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVO vo) {
        Long id = vo.getId();
        boolean flag = true;
        List<PurchaseItemDoneVO> items = vo.getItems();
        if (items != null) {
            // 1.改变采购单的状态
            for (PurchaseItemDoneVO item : items) {
                if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ERROR.getCode()){
                    // 采购项出现了问题
                    flag = false;
                }
            }
            if (flag){
                this.update(new UpdateWrapper<PurchaseEntity>().set("status",WareConstant.PurchaseStatusEnum.FINISH.getCode()).set("update_time", new Date()).eq("id",id));
            }else {
                this.update(new UpdateWrapper<PurchaseEntity>().set("status",WareConstant.PurchaseStatusEnum.ERROR.getCode()).set("update_time", new Date()).eq("id",id));
            }
            items.stream().forEach( item -> {
                Long itemId = item.getItemId();
                Integer status = item.getStatus();
                if (status == WareConstant.PurchaseDetailStatusEnum.ERROR.getCode()){
                    // 打印错误原因
                    log.error(item.getReason());
                }
                if (status == WareConstant.PurchaseDetailStatusEnum.FINISH.getCode()){
                    // 2.将采购成功的采购项入库
                    PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(itemId);
                    Long skuId = purchaseDetailEntity.getSkuId();
                    Long wareId = purchaseDetailEntity.getWareId();
                    Integer skuNum = purchaseDetailEntity.getSkuNum();
                    wareSkuService.addStock(skuId,wareId,skuNum);
                }
                // 3.改变采购项的状态
                purchaseDetailService.update(new UpdateWrapper<PurchaseDetailEntity>().set("status",status).eq("id",itemId));
            });
        }



    }

}