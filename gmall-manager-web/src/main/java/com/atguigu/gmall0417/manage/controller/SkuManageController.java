package com.atguigu.gmall0417.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0417.bean.SkuInfo;
import com.atguigu.gmall0417.bean.SpuImage;
import com.atguigu.gmall0417.bean.SpuSaleAttr;
import com.atguigu.gmall0417.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManageController {
    @Reference
    private ManageService manageService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        //调用服务处查询数据
        return manageService.getSpuImageList(spuId);
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    @ResponseBody
    // @ResponseBody也可以不写
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }

}
