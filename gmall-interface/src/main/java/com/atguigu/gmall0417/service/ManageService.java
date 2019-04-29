package com.atguigu.gmall0417.service;


import com.atguigu.gmall0417.bean.*;

import java.util.List;

public interface ManageService {
    //查询所有分类
    List<BaseCatalog1> getCatalog1();

    //根据一级分类Id查询二级分类
    List<BaseCatalog2>getCatalog2(String catalog1Id);
    //根据二级分类Id查询三级分类
    List<BaseCatalog3>getCatalog3(String catalog2Id);
    //根据三级分类Id查询平台属性列表
    List<BaseAttrInfo> getAttrList(String catalog3Id);
    //保存平台属性，平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    BaseAttrInfo getAttrInfo(String attrId);
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);
//    查询所有的销售属性名称
    List<BaseSaleAttr> getBaseSaleAttrList();
//保存
    void saveSpuInfo(SpuInfo spuInfo);
    //查询所有图片image列表
    List<SpuImage> getSpuImageList(String spuId);
    //根据spuId查询SpuSaleAttr集合
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
    //保存sku信息
    void saveSkuInfo(SkuInfo skuInfo);
    //获取skuinfo
    SkuInfo getSkuInfo(String skuId);
    //根据skuinfo查询list（SpuSaleAttr）
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    //根据spuid查询销售属性值的集合
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
