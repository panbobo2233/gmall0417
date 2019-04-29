package com.atguigu.gmall0417.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0417.bean.BaseAttrInfo;
import com.atguigu.gmall0417.bean.BaseSaleAttr;
import com.atguigu.gmall0417.bean.SpuInfo;
import com.atguigu.gmall0417.service.ManageService;
import org.apache.catalina.LifecycleState;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){
        //调用后台
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return manageService.getSpuInfoList(spuInfo);
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public  String saveSpuInfo(SpuInfo spuInfo){
        //调用服务处进行添加数据
        manageService.saveSpuInfo(spuInfo);
        return  "success";
    }

    @RequestMapping(value = "attrInfoList",method = RequestMethod.GET)
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

}
