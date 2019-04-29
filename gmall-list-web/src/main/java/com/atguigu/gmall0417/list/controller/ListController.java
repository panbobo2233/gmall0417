package com.atguigu.gmall0417.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0417.bean.BaseAttrInfo;
import com.atguigu.gmall0417.bean.BaseAttrValue;
import com.atguigu.gmall0417.bean.SkuLsParams;
import com.atguigu.gmall0417.bean.SkuLsResult;
import com.atguigu.gmall0417.service.ListService;
import com.atguigu.gmall0417.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //要想显示页面这个不能要
    //@ResponseBody
    public String list(SkuLsParams skuLsParams, HttpServletRequest request, Model model){
        //设置每页显示的条数
        skuLsParams.setPageSize(2);
        SkuLsResult skuLsResult = listService.search(skuLsParams);



        //对象转换成字符串
        String s = JSON.toJSONString(skuLsResult);
        System.out.println(s);


        //从es中取得页面平台属性值的id集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        System.out.println(attrValueIdList );
        //从平台属性值的id进行查询平台属性值的名称，查询平台属性名。 manageservice.getAttrList(String catalog3Id)
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);



        //制作url连接
        String urlParam = makeUrlParam(skuLsParams);

        //面包屑功能，声明一个集合存储面包屑
        // 已选的属性值列表
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();


        //过滤重复属性值 循环attrlist--skulsparams.getvalueid()页面得到的valueid比较结果如相同，则将数据进行remove
        //集合一 能否在遍历的过程中进行删除集合中的数据 foreach不能但是迭代器可以
        for (Iterator<BaseAttrInfo> iterator=attrList.iterator();iterator.hasNext();){
            BaseAttrInfo baseAttrInfo = iterator.next();
            //平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //循环比较
            for (BaseAttrValue baseAttrValue : attrValueList){
                //取得平台属性值对象
                baseAttrValue.setUrlParam(urlParam);
                //从http://list.gmall.com/list.html?catalog3Id=61&valueId=82&valueId=83&valueId=83
                if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                    //取得url中的每一个平台属性值id
                    for (String valueId : skuLsParams.getValueId()) {
                        //如果平台属性值id 跟 点击的平台属性值id相同则删除
                        if(valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            //取出属性名：属性值 将其添加到集合中，在页面循环显示
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();

                            //（属性名:属性值）看成一个整体付给setvaluename()
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            //做一个去重复
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            //面包屑中的Url
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValuesList.add(baseAttrValueSelected);

                        }
                    }
                }
            }





        }

        //将skulsinfo列表进行保存，加上request容器（或者model）
        request.setAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());

        //将平台属性名集合存储，然后前台显示即可
        request.setAttribute("attrList",attrList);

        //将urlParam保存到页面属性值的href属性
        request.setAttribute("urlParam",urlParam);

        //保存关键字keyword
        request.setAttribute("keyword",skuLsParams.getKeyword());

        //目前pagesize给的是3
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());

        //存储面包屑集合
        request.setAttribute("baseAttrValuesList",baseAttrValuesList);
        //返回页面
        return "list";
    }

    //制作url的方法
    //使用可变数组是因为第一次搜索的时候可能没有这个数组，注意可变数组要放在最后，不然可能报错
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {

        //参数传递 分为一个参数，多个参数：
        //一个参数：连接后面加？
        //多个参数：第一个是？第二个后续都是&连接
        String urlParam = "";
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        /**
         * 数组长度：length
         * 字符串长度length()
         * 集合 size()
         * 文件 length()
         */
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            //循环匹配
            for (int i=0;i<skuLsParams.getValueId().length;i++){
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueIds!=null && excludeValueIds.length>0){
                    //为啥是0 因为每一次只能点一次
                    String excludeValueId = excludeValueIds[0];
                    if (valueId.equals(excludeValueId)){
                        continue;
                    }
                }
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }

}
