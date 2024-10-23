package com.zzy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.product.entity.AttrEntity;
import com.zzy.mall.product.vo.AttrGroupRelationVO;
import com.zzy.mall.product.vo.AttrResponseVO;
import com.zzy.mall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO vo);

    PageUtils queryBasePage(Map<String, Object> params, Long categlogId, String attrType);

    AttrResponseVO getAttrInfo(Long attrId);

    void updateBaseAttr(AttrVO attr);

    void removeByIdsDetails(List<Long> list);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void deleteRelation(AttrGroupRelationVO[] vos);

    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrGroupId);
}

