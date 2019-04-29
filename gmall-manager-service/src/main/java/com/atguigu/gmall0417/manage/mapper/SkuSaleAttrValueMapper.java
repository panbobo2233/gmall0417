package com.atguigu.gmall0417.manage.mapper;


import com.atguigu.gmall0417.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    //根据spuid查询销售属性值id
     List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
