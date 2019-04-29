package com.atguigu.gmall0417.passport.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {
    /**
     *
     * @param key  ATGUIGU_GMALL_KEY
     * @param param 将用户信息放入map中
     * @param salt 盐=服务器ip地址
     * @return
     */
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            key+=salt;
        }
        //key = ATGUIGU_GMALL_KEY
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);
        //用户信息放入
        jwtBuilder = jwtBuilder.setClaims(param);
        //制作token
        String token = jwtBuilder.compact();
        return token;

    }


    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
