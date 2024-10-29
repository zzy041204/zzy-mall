package com.zzy.mall.product.service.impl;

import com.zzy.mall.product.service.CategoryBrandRelationService;
import com.zzy.mall.product.vo.Catalog2VO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.Query;

import com.zzy.mall.product.dao.CategoryDao;
import com.zzy.mall.product.entity.CategoryEntity;
import com.zzy.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询所有的类别数据 将数据封装成树形结构 便于前端使用
     *
     * @param params
     * @return
     */
    @Override
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params) {
        //1.查询所有的商品分类信息
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //2.将商品分类信息拆解为树形结构
        // 遍历所有的大类 parent_cid=0
        List<CategoryEntity> list = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    //根据大类找到所有的小类
                    categoryEntity.setChildren(getCategoryChildren(categoryEntity, categoryEntities));
                    return categoryEntity;
                }).sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                }).collect(Collectors.toList());
        // 根据大类找到对应的所有小类
        return list;
    }

    /**
     * 逻辑批量删除
     *
     * @param ids
     */
    @Override
    public void removeCategoryByIds(List<Long> ids) {
        // 1.检查类别数据是否在其他业务中使用

        //批量逻辑删除
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        // 更新类别名称
        this.updateById(category);
        if (StringUtils.isNotBlank(category.getName())) {
            // 同步更新级联的数据
            Long catId = category.getCatId();
            String name = category.getName();
            categoryBrandRelationService.updateCatelogName(catId, name);
            // 同步更新其他冗余的数据
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Category() {
        List<CategoryEntity> list = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return list;
    }

    /**
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     *
     * @return
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatalog2JSON() {
        // 获取所有一级分类的编号
        List<CategoryEntity> level1Category = this.getLevel1Category();
        // 把一级分类的数据转换为Map容器 key是一级分类的编号 value就是一级分类对应的二级分类
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(key -> key.getCatId().toString()
                , value -> {
                    List<Catalog2VO> catalog2VOS = null;
                    List<CategoryEntity> catalog2List = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", value.getCatId()));
                    if (catalog2List.size() > 0 && catalog2List != null) {
                        List<Catalog2VO> catalog2VOList = catalog2List.stream().map(category2Entity -> {
                            Catalog2VO catalog2VO = new Catalog2VO(value.getCatId().toString(), null, category2Entity.getCatId().toString(), category2Entity.getName());
                            List<Catalog2VO.Catalog3VO> catalog3VOS = null;
                            // 根据二级分类的ID 找到对应的三级分类信息
                            List<CategoryEntity> catalog3List = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", category2Entity.getCatId()));
                            if (catalog3List.size() > 0 && catalog3List != null) {
                                List<Catalog2VO.Catalog3VO> collect = catalog3List.stream().map(category3Entity -> {
                                    Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(category2Entity.getCatId().toString(), category3Entity.getCatId().toString(), category3Entity.getName());
                                    return catalog3VO;
                                }).collect(Collectors.toList());
                                catalog3VOS = collect;
                            }
                            catalog2VO.setCatalog3List(catalog3VOS);
                            return catalog2VO;
                        }).collect(Collectors.toList());
                        catalog2VOS = catalog2VOList;
                    }
                    return catalog2VOS;
                }));
        return map;
    }

    /**
     * 225,22,2
     *
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        Long parentCid = entity.getParentCid();
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }
        return paths;
    }

    /**
     * 查找该大类下的所有小类 递归查找
     *
     * @param categoryEntity   某个大类
     * @param categoryEntities 所有的类别数据
     * @return
     */
    private List<CategoryEntity> getCategoryChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(entity -> {
            //根据大类找到其直属的小类
            //注意Long数据比较 不在-128-127之间 会new Long() 对象
            return entity.getParentCid().equals(categoryEntity.getCatId());
        }).map(entity -> {
            //根据小类递归找到小小类
            entity.setChildren(getCategoryChildren(entity, categoryEntities));
            return entity;
        }).sorted((entity1, entity2) -> {
            return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }


}