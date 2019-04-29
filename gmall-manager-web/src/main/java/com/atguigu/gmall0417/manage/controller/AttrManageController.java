package com.atguigu.gmall0417.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0417.bean.*;
import com.atguigu.gmall0417.service.ListService;
import com.atguigu.gmall0417.service.ManageService;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

//平台属性的数据显示放在AttrManageController
@Controller
public class AttrManageController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        //调用服务层查询所有数据
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        //调用服务层查询所有数据
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        //调用服务层查询所有数据
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id){
        //调用服务层查询所有数据
        return manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        //平台属性名的id 查询到baseattrinfo对象
        BaseAttrInfo attrInfo =manageService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }

    //这里GET是因为需要在url加上skuid
    @RequestMapping(value = "onSale",method = RequestMethod.GET)
    @ResponseBody
    public void onSale(String skuId){
        //调用服务层
        //根据skuid查询skuinfo信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //数据从哪里来？skuinfo
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //属性赋值！工具类
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //调用服务层
        listService.saveSkuInfo(skuLsInfo);
    }


}
