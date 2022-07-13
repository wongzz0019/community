package com.hzz.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解可以用在方法上
@Target(ElementType.METHOD)
//声明在程序运行时才有效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {


}
