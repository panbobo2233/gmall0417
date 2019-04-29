package com.atguigu.gmall0417.manage.mapper;

import com.atguigu.gmall0417.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    // 根据三级分类id查询属性表
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);

    //根据平台属性值id查询平台属性集合
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
