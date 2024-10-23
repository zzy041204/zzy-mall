package com.zzy.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zzy.mall.common.valid.groups.AddGroupsInterface;
import com.zzy.mall.common.valid.groups.UpdateGroupsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.zzy.mall.product.entity.BrandEntity;
import com.zzy.mall.product.service.BrandService;
import com.zzy.mall.common.utils.PageUtils;
import com.zzy.mall.common.utils.R;


/**
 * 品牌
 *
 * @author zzy
 * @email 16671131204@163.com
 * @date 2024-10-07 15:30:18
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("/all")
    public R queryAllBrand(){
        BrandEntity entity = new BrandEntity();
        entity.setName("华为");
        return R.ok().put("brands",entity);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroupsInterface.class) @RequestBody BrandEntity brand){
		brandService.save(brand);
        return R.ok();
    }



    /*@RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
        if(result.hasErrors()){
            //提交的字段经过JSR303检验后有非法字段
            Map<String,String> map = new HashMap<>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            for (FieldError error : fieldErrors) {
                //获取非法的field
                String field = error.getField();
                //获取非法field的提示信息
                String defaultMessage = error.getDefaultMessage();
                map.put(field,defaultMessage);
            }
            return R.error(400,"提交的品牌表单数据不合法").put("data",map);
        }
        brandService.save(brand);
        return R.ok();
    }*/

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroupsInterface.class) @RequestBody BrandEntity brand){
		//brandService.updateById(brand);
        brandService.updateDetail(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
