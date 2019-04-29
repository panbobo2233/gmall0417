package com.atguigu.gmall0417.passport;

import com.atguigu.gmall0417.passport.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test01(){
        String key = "atguigu";
        String ip="192.168.222.130";
        Map map = new HashMap();
        map.put("userId","1001");
        map.put("nickName","marry");
        String token = JwtUtil.encode(key, map, ip);
        System.out.println(token+"====");
        //解密 --key+salt必须一致，否则解密失败
        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.222.130");
        System.out.println(decode+"====");
    }

}
