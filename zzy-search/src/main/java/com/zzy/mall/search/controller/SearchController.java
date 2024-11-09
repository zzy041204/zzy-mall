package com.zzy.mall.search.controller;

import com.zzy.mall.search.service.ZzySearchService;
import com.zzy.mall.search.vo.SearchParam;
import com.zzy.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    ZzySearchService zzySearchService;

    @GetMapping(value = {"/list.html","/","index.html"})
    public String listPage(SearchParam param, Model model){
        // 通过对应的Service根据传递过来相关的信息去ES检索对应的数据
        SearchResult search = zzySearchService.search(param);
        model.addAttribute("result",search);
        return "index";
    }


}
