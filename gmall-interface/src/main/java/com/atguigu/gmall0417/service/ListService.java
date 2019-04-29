package com.atguigu.gmall0417.service;

import com.atguigu.gmall0417.bean.SkuLsInfo;
import com.atguigu.gmall0417.bean.SkuLsParams;
import com.atguigu.gmall0417.bean.SkuLsResult;

public interface ListService {
     //skulsinfo -对es数据进行封装的对象
     void saveSkuInfo(SkuLsInfo skuLsInfo);
     //根据用户输入的参数返回封装好的结果集
     SkuLsResult search(SkuLsParams skuLsParams);

     //为当前的商品增加热度
     void incrHotScore(String skuId);
}
