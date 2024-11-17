package com.zzy.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.zzy.mall.cart.feign.ProductFeignService;
import com.zzy.mall.cart.interceptor.AuthInterceptor;
import com.zzy.mall.cart.service.ICartService;
import com.zzy.mall.cart.vo.Cart;
import com.zzy.mall.cart.vo.CartItem;
import com.zzy.mall.cart.vo.SkuInfoVo;
import com.zzy.mall.common.constant.CartConstant;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.common.vo.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 购物车信息存储在Redis中
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Cart getCart() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        Set<Object> keys = operations.keys();
        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        for (Object k : keys) {
            String key = (String) k;
            String item = (String) operations.get(key);
            CartItem cartItem = JSON.parseObject(item, CartItem.class);
            cartItems.add(cartItem);
        }
        cart.setItems(cartItems);
        return cart;
    }

    /**
     * 把商品添加到购物车当中
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        // 如果redis中存在该商品的信息 我们只需要修改商品的数量即可
        Object o = hashOperations.get(Long.toString(skuId));
        if (o != null){
            // 说明存在了商品 修改商品数量即可
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            item.setCount(item.getCount() + num);
            hashOperations.put(Long.toString(skuId), JSON.toJSONString(item));
            return item;
        }
        CartItem item = new CartItem();
        CompletableFuture future1 = CompletableFuture.runAsync(() -> {
            R info = productService.info(skuId);
            // 1.获取商品的基本属性
            String json = (String) info.get("skuInfoJSON");
            SkuInfoVo vo = JSON.parseObject(json, SkuInfoVo.class);
            item.setSkuId(vo.getSkuId());
            item.setSpuId(vo.getSpuId());
            item.setImage(vo.getSkuDefaultImg());
            item.setCheck(true);
            item.setCount(num);
            item.setPrice(vo.getPrice());
            item.setTitle(vo.getSkuTitle());
        },threadPoolExecutor);
        CompletableFuture future2 = CompletableFuture.runAsync(() -> {
            // 2.获取商品的销售属性
            List<String> skuSaleAttrs = productService.getSkuSaleAttrs(skuId);
            item.setSkuAttr(skuSaleAttrs);
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2);
        allOf.get();

        // 3.把数据存储到redis中
        String jsonString = JSON.toJSONString(item);
        hashOperations.put(Long.toString(skuId),jsonString);
        return item;
    }

    private BoundHashOperations<String, Object, Object> getCartKeyOperation(){
        /*MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX + memberVO.getId();*/
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX + 1;
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }


}
