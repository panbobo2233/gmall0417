package com.atguigu.gmall0417.manage.mapper;

import com.atguigu.gmall0417.bean.SkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuInfoMapper extends Mapper<SkuInfo> {

    public List<SkuInfo> selectSkuInfoListBySpu(long spuId);
}