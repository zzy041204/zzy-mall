package com.zzy.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zzy.mall.product.service.CategoryBrandRelationService;
import com.zzy.mall.product.vo.Catalog2VO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

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

    //@CacheEvict(value = "category",key = "'getLevel1Category'")
    /*@Caching(evict = {@CacheEvict(value = "category",key = "'getLevel1Category'"),
            @CacheEvict(value = "category",key = "'getCatalog2JSON'")})*/
    @CacheEvict(value = "category",allEntries = true)  // 删除这个分区下的所有缓存
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

    /**
     * 查询所有商品的大类
     * "category","product" 缓存名称 起到一个分区的作用 一般按照业务来区分
     * @Cacheable({"category","product"}) 执行该方法是需要缓存的 调用该方法时 如果缓存中有数据 则该方法就不会执行
     * 如果缓存中没有数据 那么就执行该方法并把查询结果缓存起来
     * 缓存处理
     *  1.存储在Redis中的缓存数据的key是默认生成的： 缓存名称::simpleKey[]
     *  2.默认缓存的过期时间是永久
     *  3.缓存的数据默认使用的是jdk序列化机制
     * 改进
     *  1.生成的缓存数据我们需要指定自定义的缓存key 通过key属性指定 可以通过#root获取上下文相关信息
     *  2.指定缓存数据的存活时间 spring.cache.redis.time-to-live 指定过期时间
     *  3.把缓存数据保存为JSON数据
     *
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Category() {
        System.out.println("查询了数据库操作");
        long start = System.currentTimeMillis();
        List<CategoryEntity> list = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("查询消耗的时间：" + (System.currentTimeMillis() - start));
        return list;
    }

    /**
     * 根据父编号获取对应的子菜单信息
     * @param list
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> queryByParentCid(List<CategoryEntity> list,Long parentCid) {
        List<CategoryEntity> collect = list.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return collect;
    }

    /**
     * 查询所有数据并完成一级二级及三级的关联
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public Map<String, List<Catalog2VO>> getCatalog2JSON(){
        // 获取所有分类数据
        List<CategoryEntity> list = this.list();
        // 获取所有一级分类的编号
        List<CategoryEntity> level1Category = this.queryByParentCid(list,0l);
        // 把一级分类的数据转换为Map容器 key是一级分类的编号 value就是一级分类对应的二级分类
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString()
                , value -> {
                    List<Catalog2VO> catalog2VOS = null;
                    List<CategoryEntity> catalog2List = this.queryByParentCid(list,value.getCatId());
                    if (catalog2List.size() > 0 && catalog2List != null) {
                        List<Catalog2VO> catalog2VOList = catalog2List.stream().map(category2Entity -> {
                            Catalog2VO catalog2VO = new Catalog2VO(value.getCatId().toString(), null, category2Entity.getCatId().toString(), category2Entity.getName());
                            List<Catalog2VO.Catalog3VO> catalog3VOS = null;
                            // 根据二级分类的ID 找到对应的三级分类信息
                            List<CategoryEntity> catalog3List = this.queryByParentCid(list,category2Entity.getCatId());
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
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, List<Catalog2VO>>对象
     *
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalog2JSONRedis(){
        String key = "getCatalog2JSON";
        // 从redis中获取分类信息
        String catalogJSON = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(catalogJSON)){
            System.out.println("缓存没有命中");
            // 缓存中没有数据需要从数据库中查询 并存储在缓存
            Map<String, List<Catalog2VO>> json = getCatalog2JSONDbWithRedisson();
            return json;
        }else {
            System.out.println("缓存命中了");
            // 表示缓存命中了数据 从缓存中获取信息
            Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
            return stringListMap;
        }
    }

    public Map<String, List<Catalog2VO>> getCatalog2JSONDbWithRedisson() {
        String key = "getCatalog2JSON";
        // 获取分布式锁对象 锁名称要注意
        RLock myLock = redissonClient.getLock("getCatalog2JSON-lock");
        Map<String, List<Catalog2VO>> data = null;
        // 加锁成功
        myLock.lock();
        try {
            data = getDataForDB(key);
        }finally {
            myLock.unlock();
        }
        return data;
    }

    public Map<String, List<Catalog2VO>> getCatalog2JSONDbWithRedisLock() {
        String key = "getCatalog2JSON";
        String uuid = UUID.randomUUID().toString();
        // 加锁的同时设置过期时间
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if(lock){
            System.out.println("获取分布式锁成功");
            Map<String, List<Catalog2VO>> data = null;
            try {
                data = getDataForDB(key);
            } finally {
                // 通过redis的lua脚本 保证查询锁的值 和删除锁是一个原子性操作
                String scripts = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<>(scripts, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        }else {
            // 加锁失败 休眠或者重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("获取分布式锁失败");
            return getCatalog2JSONDbWithRedisLock();
        }
    }

    /**
     * 从数据库中查询操作
     * @param key
     * @return
     */
    private Map<String,List<Catalog2VO>> getDataForDB(String key){
        // 先从缓存中查询有没有数据 有就返回 否则查询数据库
        String catalogJSON = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(catalogJSON)){
            Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(stringRedisTemplate.opsForValue().get(key), new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
            return stringListMap;
        }
        System.out.println("-------------->查询数据库操作");
        // 获取所有分类数据
        List<CategoryEntity> list = this.list();
        // 获取所有一级分类的编号
        List<CategoryEntity> level1Category = this.queryByParentCid(list,0l);
        // 把一级分类的数据转换为Map容器 key是一级分类的编号 value就是一级分类对应的二级分类
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString()
                , value -> {
                    List<Catalog2VO> catalog2VOS = null;
                    List<CategoryEntity> catalog2List = this.queryByParentCid(list,value.getCatId());
                    if (catalog2List.size() > 0 && catalog2List != null) {
                        List<Catalog2VO> catalog2VOList = catalog2List.stream().map(category2Entity -> {
                            Catalog2VO catalog2VO = new Catalog2VO(value.getCatId().toString(), null, category2Entity.getCatId().toString(), category2Entity.getName());
                            List<Catalog2VO.Catalog3VO> catalog3VOS = null;
                            // 根据二级分类的ID 找到对应的三级分类信息
                            List<CategoryEntity> catalog3List = this.queryByParentCid(list,category2Entity.getCatId());
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
        if (map == null){
            // 防止缓存穿透
            stringRedisTemplate.opsForValue().set(key,"1",5, TimeUnit.SECONDS);
        }else {
            String jsonString = JSON.toJSONString(map);
            // 防止缓存雪崩
            stringRedisTemplate.opsForValue().set(key, jsonString,10,TimeUnit.MINUTES);
        }
        return map;
    }

    /**
     * 从数据库查询的结果
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, List<Catalog2VO>>对象
     *
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalog2JSONDbWithLocalLock() {
        String key = "getCatalog2JSON";
        synchronized (this){
            // 先从缓存中查询有没有数据 有就返回 否则查询数据库
            String catalogJSON = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(catalogJSON)){
                Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(stringRedisTemplate.opsForValue().get(key), new TypeReference<Map<String, List<Catalog2VO>>>() {
                });
                return stringListMap;
            }
            System.out.println("-------------->查询数据库操作");
            // 获取所有分类数据
            List<CategoryEntity> list = this.list();
            // 获取所有一级分类的编号
            List<CategoryEntity> level1Category = this.queryByParentCid(list,0l);
            // 把一级分类的数据转换为Map容器 key是一级分类的编号 value就是一级分类对应的二级分类
            Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString()
                    , value -> {
                        List<Catalog2VO> catalog2VOS = null;
                        List<CategoryEntity> catalog2List = this.queryByParentCid(list,value.getCatId());
                        if (catalog2List.size() > 0 && catalog2List != null) {
                            List<Catalog2VO> catalog2VOList = catalog2List.stream().map(category2Entity -> {
                                Catalog2VO catalog2VO = new Catalog2VO(value.getCatId().toString(), null, category2Entity.getCatId().toString(), category2Entity.getName());
                                List<Catalog2VO.Catalog3VO> catalog3VOS = null;
                                // 根据二级分类的ID 找到对应的三级分类信息
                                List<CategoryEntity> catalog3List = this.queryByParentCid(list,category2Entity.getCatId());
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
            if (map == null){
                // 防止缓存穿透
                stringRedisTemplate.opsForValue().set(key,"1",5, TimeUnit.SECONDS);
            }else {
                String jsonString = JSON.toJSONString(map);
                // 防止缓存雪崩
                stringRedisTemplate.opsForValue().set(key, jsonString,10,TimeUnit.MINUTES);
            }
            return map;
        }
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