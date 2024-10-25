package com.zzy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.ware.entity.PurchaseEntity;
import com.zzy.mall.ware.vo.MergeVO;
import com.zzy.mall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 17:18:29
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceive(Map<String, Object> params);

    Integer merge(MergeVO mergeVO);

    void received(List<Long> ids);

    void done(PurchaseDoneVO vo);
}

