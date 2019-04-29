package com.atguigu.gmall0417.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//spring中的xx.xml
@Configuration
public class RedisConfig {
    //host port属于服务器的地址和端口号，不要在程序里直接写死
    //通过配置文件的形式将host port database赋值 applications.properties
    @Value("${spring.redis.host:disabled}")
    private String host;//如果配置文件中没有spring.redis.host,则host为disabled

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    //application.xml <bean class="com.atguigu.gmall0417.config.RedisConfig"/>
    //通过bean配上redisutil的一个对象
    @Bean
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){ //说明配置文件没有，host没有后续的其他的也没有
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
        redisUtil.initJedisPool(host,port,database);//这里已经初始化了，所以接下来可以用
        return redisUtil;

    }
}
