package com.zzy.mall.cart.service;

import com.zzy.mall.cart.vo.Cart;
import com.zzy.mall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 购物车的Service接口
 */
public interface ICartService {

    public Cart getCart();

    CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    List<CartItem> getUserCartItems();

}
