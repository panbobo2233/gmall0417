package com.atguigu.gmall0417.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//target表示这个注解在什么地方生效
//retention表示注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
//课堂（笔记有）
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    boolean autoRedirect() default true;
}
