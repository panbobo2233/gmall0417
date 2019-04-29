package com.atguigu.gmall0417.service;

import com.atguigu.gmall0417.bean.CartInfo;

import java.util.List;

public interface CartService {
    //方法：一个是返回值一个是参数
    void  addToCart(String skuId,String userId,Integer skuNum);

    List<CartInfo> getCartList(String userId);

    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);
    //商品选中状态
    void checkCart(String skuId, String isChecked, String userId);
    //skuid:redis---field  userid:redis:key
   // void  addToCart(String skuId, String userId, CartInfo cartInfo);
}
