package com.zzy.mall.cart.controller;

import com.zzy.mall.cart.interceptor.AuthInterceptor;
import com.zzy.mall.cart.service.ICartService;
import com.zzy.mall.cart.vo.Cart;
import com.zzy.mall.cart.vo.CartItem;
import com.zzy.mall.common.vo.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    ICartService cartService;

    @GetMapping("/getUserItems")
    @ResponseBody
    public List<CartItem> getUserCartItems() {
        return cartService.getUserCartItems();
    }


    @GetMapping("/cartList")
    public String queryCart(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 加入购物车
     * @param skuId
     * @return
     */
    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId
            , @RequestParam("num") Integer num
            , Model model) {
        // 把商品添加到购物车
        CartItem item = null;
        try {
            item = cartService.addCart(skuId,num);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("item",item);
        return "success";
    }

}
