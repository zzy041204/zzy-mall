package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.AttrGroupEntity;
import com.zzy.mall.product.vo.AttrGroupWithAttrVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrVO> getAttrGroupWithAttrsByCatelogId(Long catelogId);
}

