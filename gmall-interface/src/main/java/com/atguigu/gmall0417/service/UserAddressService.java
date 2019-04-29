package com.atguigu.gmall0417.service;

import com.atguigu.gmall0417.bean.UserAddress;

import java.util.List;

public interface UserAddressService {
    List<UserAddress> findByUserId(String userId);
}
