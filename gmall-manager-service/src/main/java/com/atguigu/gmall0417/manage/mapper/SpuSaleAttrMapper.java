package com.atguigu.gmall0417.manage.mapper;

import com.atguigu.gmall0417.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    //根据spuId查询SpuSaleAttr...获取数据的时候命名取get 如果跟db进行交互使用select(所以xml中的id为select 而controller的方法叫get)
    public List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);
    //根据skuid,spuid，查找销售属性，销售属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId,long spuId);
}
