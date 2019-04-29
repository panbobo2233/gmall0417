package com.atguigu.gmall0417.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0417.bean.UserInfo;
import com.atguigu.gmall0417.passport.util.JwtUtil;
import com.atguigu.gmall0417.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    //需要用户模块的service
    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    private String key;


    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){

        //这里的header是当时nginx设置的时候加的
        // 取得linux服务器的ip地址
        String ip  = request.getHeader("X-forwarded-for");

        //用户名+密码进行验证
        UserInfo loginUser = userInfoService.login(userInfo);
        if (loginUser!=null){
            //做token -JWT
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",loginUser.getId());
            map.put("nickName", loginUser.getNickName());
            String token = JwtUtil.encode(key, map, ip);
            return token;
        }else{
            return "fail";
        }

    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //取得token
        String token = request.getParameter("token");
        String salt = request.getHeader("X-forwarded-for");
        //对token进行解密
        Map<String,Object> map = JwtUtil.decode(token,key,salt);
        //map中的userid跟redis中进行匹配
        if (map!=null && map.size()>0){
            String userId = (String) map.get("userId");
            //调用认证方法将userid传入进去
            UserInfo userInfo = userInfoService.verify(userId);
            if (userInfo!=null){
                return "success";
            }else{
                return "fail";
            }
        }
        return "fail";
    }




}
