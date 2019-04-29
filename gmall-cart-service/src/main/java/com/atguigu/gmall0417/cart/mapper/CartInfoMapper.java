package com.atguigu.gmall0417.cart.mapper;

import com.atguigu.gmall0417.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    //为商品验价做准备
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
