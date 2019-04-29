package com.atguigu.gmall0417.usermanager.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0417.bean.UserAddress;
import com.atguigu.gmall0417.bean.UserInfo;
import com.atguigu.gmall0417.config.RedisUtil;
import com.atguigu.gmall0417.service.UserInfoService;
import com.atguigu.gmall0417.usermanager.mapper.UserAddressMapper;
import com.atguigu.gmall0417.usermanager.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import javax.swing.*;
import java.util.List;

//使用alibb的那个注解
@Service
public class UserInfoServiceImpl implements UserInfoService {
    //定义用户信息
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60;

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        List<UserAddress> addressList = null;
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        addressList = userAddressMapper.select(userAddress);
        return addressList;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //页面传递admin-123
        //数据库里的密码是加密过的
        //将123加密
        String passwd = userInfo.getPasswd();
        //123->202cb....
        String newPwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPwd);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        //如果登录成功，则存入redis
        Jedis jedis = redisUtil.getJedis();

        if (info!=null){
            //定义key user:1:info
            String userKey = userKey_prefix + info.getId()+userinfoKey_suffix;
            //做存储数据
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));

            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //根据userId去redis查数据
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String key = userKey_prefix+userId+userinfoKey_suffix;
        //根据key获取数据
        String userJson = jedis.get(key);
        //延长时效
        //因为认证操作相当于其他模块在登陆，所以需要延长用户过期时间
        jedis.expire(key,userKey_timeOut);
        if (userJson!=null && userJson.length()>0){
            //将字符串转换为对象
            UserInfo userInfo = JSON.parseObject(userJson,UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
