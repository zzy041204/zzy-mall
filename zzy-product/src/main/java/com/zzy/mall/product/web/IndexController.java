package com.zzy.mall.product.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzy.mall.product.entity.CategoryEntity;
import com.zzy.mall.product.service.CategoryService;
import com.zzy.mall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html","/home","/home.html"})
    public String index(Model model) {
        // 查询出所有一级分类的信息
        List<CategoryEntity> list = categoryService.getLevel1Category();
        model.addAttribute("categories", list);
        // classpath:/templates
        //.html
        return "index";
    }

    // index/catalog.json
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalog2JSON(){
        Map<String, List<Catalog2VO>> map = categoryService.getCatalog2JSON();
        return map;
    }

}
