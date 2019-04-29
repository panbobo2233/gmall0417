package com.atguigu.gmall0417.usermanager.controller;

import com.atguigu.gmall0417.bean.UserInfo;
import com.atguigu.gmall0417.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> getAll(){
     return userInfoService.findAll();
    }
//    @ResponseBody : 返回json字符串，并将数据显示在页面上
//    @GetMapping("/findAll")
//    public ResponseEntity<List<UserInfo>> getAll(){
//         List<UserInfo> userInfoListAll = userInfoService.findAll();
//        return  ResponseEntity.ok(userInfoListAll);
//    }


}
