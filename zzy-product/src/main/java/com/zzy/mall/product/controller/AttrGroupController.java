package com.zzy.mall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zzy.mall.product.entity.AttrEntity;
import com.zzy.mall.product.service.AttrAttrgroupRelationService;
import com.zzy.mall.product.service.AttrService;
import com.zzy.mall.product.service.CategoryService;
import com.zzy.mall.product.vo.AttrGroupRelationVO;
import com.zzy.mall.product.vo.AttrGroupWithAttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzy.mall.product.entity.AttrGroupEntity;
import com.zzy.mall.product.service.AttrGroupService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.R;


/**
 * 属性分组
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    //app/product/attrgroup/225/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        // 根据三级分类的编号获取属性组和属性组的基本属性信息
        List<AttrGroupWithAttrVO> list = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", list);
    }

    //attr/relation
    @PostMapping("/attr/relation")
    public R saveBatch(@RequestBody List<AttrGroupRelationVO> vos){
        attrAttrgroupRelationService.saveVOS(vos);
        return R.ok();
    }

    // 4/noattr/relation
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrGroupId") Long attrGroupId,@RequestParam Map<String, Object> params) {
        PageUtils pageUtils = attrService.getNoAttrRelation(params,attrGroupId);
        return R.ok().put("page", pageUtils);
    }


    //attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrGroupRelationVO[] vos){
        attrService.deleteRelation(vos);
        return R.ok().put("data",null);
    }

    //product/attrgroup/5/attr/relation
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data",list);
    }


    /**
     * 列表
     * 分页查询
     * 前端提交请求需要封装对应的分页数据
     * {
     *     page:1, //当前页
     *     limit:10, //每页显示的条数
     *     sidx:"id", //排序的字段
     *     order:"asc/des", //排序的方式
     *     key:"小米" //查询的关键字
     * }
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable(("catelogId")) Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        //根据找到的属性组对应的分类ID 找到对应的[一级,二级,三级]数据
        Long catelogId = attrGroup.getCatelogId();
        Long[] paths = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(paths);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }


}
