package com.atguigu.gmall0417.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0417.bean.SkuInfo;
import com.atguigu.gmall0417.bean.SkuSaleAttrValue;
import com.atguigu.gmall0417.bean.SpuSaleAttr;
import com.atguigu.gmall0417.config.LoginRequire;
import com.atguigu.gmall0417.service.ListService;
import com.atguigu.gmall0417.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    private ManageService manageService;

    @Reference
    ListService listService;

    @RequestMapping("/{skuId}.html")
    @LoginRequire(autoRedirect = false)
    public String skuInfoPage(@PathVariable(value = "skuId") String skuId, HttpServletRequest request) {
        //商品详情,是根据页面传递过来的商品Id进行查找！动态，如何变成动态？
        //springmvc讲的
        System.out.println("skuId="+skuId);
        //根据skuid进行查找数据，数据库那张表？skuinfo调用后台manageservice
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //存入，给前台使用
        request.setAttribute("skuInfo",skuInfo);
        //request.setAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> spuSaleAttrList = manageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        //做拼接字符串的功能
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu= manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        //开始拼串
        String jsonKey="";
        HashMap<String,String> map = new HashMap<>();
        for(int i=0;i<skuSaleAttrValueListBySpu.size();i++){//注意i是从0开始所以下面+1
            //取得集合中的值
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if (jsonKey.length()!=0){
                jsonKey+="|";
            }
            //jsonKey+=108 jsonKey+=108|110
            jsonKey+=skuSaleAttrValue.getSaleAttrValueId();
            //什么时候将jsonKey重置？什么时候结束拼接
            //当前的skuid：skuSaleAttrValue.getSkuId()
            if ((i+1)==skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                map.put(jsonKey,skuSaleAttrValue.getSkuId());
                jsonKey="";
            }

        }
        //转换json字符串
        String valuesSkuJson = JSON.toJSONString(map);
        System.out.println("valuesSkuJson="+valuesSkuJson);
        request.setAttribute("valuesSkuJson",valuesSkuJson);


        //调用热度排名
        listService.incrHotScore(skuId);

        return "item";
    }

}
