package com.atguigu.gmall0417.config;


import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


//实现，继承
//被spring扫描到
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    //进入控制器之前
    //object handle是指能先获取方法中的所有东西
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //将生产token放入cookie中
        //http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.F_mFkAdJx8RmkKHG4jPCaeRd8ZecOBSjJz-D6p-tsPM
        String token = request.getParameter("newToken");
        if (token!=null){
            //将token放入cookie
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);


        }
        //直接访问登录页面,当用户直接进去其他项目模块
        if (token==null){
            //如果用户登录了，访问其他页面的时候不会有newToken,那么token可能已经在cookie中存在了
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        //已经登录的token，也就是cookie中的token
        if (token!=null){
            //取token中的有效数据，要解密
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }

        //Object handle
        //获取方法 获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //不为空说明类上有注解
        if (methodAnnotation!=null){
            //必须要登录【调用认证】
            String remoteAddr = request.getHeader("x-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            //说明当前用户已经登录 保存 userId
            if("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId =(String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{
                //当前的注解是否是true
                if(methodAnnotation.autoRedirect()){
                    //认证失败！重新登录！（要取得当前的url便于登录后恢复）
                    //http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F28.html
                    String  requestURL = request.getRequestURL().toString();//http://item.gmall.com/28.html

                    //进行加密编码
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }

            }
        }
        return true;
    }

    //解密
    private Map getUserMapByToken(String token) {
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        //数组一map
        //字符串
        String  str=null;
        try {
            str = new String(bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //字符串-> map
        Map map = JSON.parseObject(str,Map.class);
        return map;
    }


}
