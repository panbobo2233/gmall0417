package com.atguigu.gmall0417.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0417.bean.CartInfo;
import com.atguigu.gmall0417.bean.SkuInfo;
import com.atguigu.gmall0417.config.CookieUtil;
import com.atguigu.gmall0417.config.LoginRequire;
import com.atguigu.gmall0417.service.CartService;
import com.atguigu.gmall0417.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {
    @Reference
    private ManageService manageService;

    @Reference
    private CartService cartService;

    //建立一个操作cookie的类
    @Autowired
    private CartCookieHandler cartCookieHandler;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //获取skuNum
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        //userId哪里取得？（在拦截器有）
        String userId = (String) request.getAttribute("userId");
        //判断当前用户当前是否登录
        if (userId!=null){
            //添加完数据，放入redis，addToCart该方法是在登录之后使用
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            //未登录 cookie放入购物车
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        //添加成功页面需要skuinfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //展示：分为登录，未登录
        // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
        // 没有登录，从cookie中取得
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            //从redis-db 查看当前是谁的购物车
            List<CartInfo> cartInfoList = null;
            //会有合并的操作，cookie中的数据没有，应该删除,  cookie 跟redis合并
            //先获取cookie中的数据
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            if (cartListCK!=null && cartListCK.size()>0){
                //写一个方法合并 如果ck有就合并
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                //cookie中要删除商品数据
                cartCookieHandler.deleteCartCookie(request,response);
            }else {
                //cookie中没有数据，直接从redis取得数据
                cartInfoList = cartService.getCartList(userId);
            }
            request.setAttribute("cartInfoList",cartInfoList);
        }else {
            //cookie查看--单纯的不作任何操作
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartInfoList",cartList);
        }
        return "cartList";
    }
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
//        同样这里要区分，用户登录和未登录状态。
//        如果登录，修改缓存中的数据，如果未登录，修改cookie中的数据。
        //在cartlist.html 的js checkSku中
        // var param="isChecked="+isCheckedFlag+"&"+"skuId="+skuId;
        //$.post("checkCart",param,function (data) {
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        //得到userid
        String userId=(String) request.getAttribute("userId");
        if (userId!=null){
            //操作redis
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            //操作cookies 同时不需要userid因为未登录
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        //选中的商品列表，redis，cookie中是不是也有可能有选中的！
        List<CartInfo> cookieHandlerCartList = cartCookieHandler.getCartList(request);
        if (cookieHandlerCartList!=null && cookieHandlerCartList.size()>0){
            //调用合并方法
            cartService.mergeToCartList(cookieHandlerCartList,userId);
            //删除cookie数据
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }


}
