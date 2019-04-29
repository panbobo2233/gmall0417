package com.atguigu.gmall0417.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0417.manage.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall0417")
public class GmallManagerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallManagerServiceApplication.class, args);
    }

}
