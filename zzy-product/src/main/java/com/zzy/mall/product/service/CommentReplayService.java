package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

