package com.zzy.mall.product.web;

import com.zzy.mall.product.service.SkuInfoService;
import com.zzy.mall.product.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * 商品详情的控制器
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {
        ItemVO itemVO = skuInfoService.item(skuId);
        model.addAttribute("item", itemVO);
        return "item";
    }

}
