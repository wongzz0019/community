package com.hzz.controller.interceptor;

import com.hzz.annotation.LoginRequired;
import com.hzz.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //参数Object handler 是拦截目标
        //判断是不是方法，我们要拦截方法，
        if (handler instanceof HandlerMethod) {
            //Object转型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取拦截对象
            Method method = handlerMethod.getMethod();
            //获取有 @LoginRequired 注解的方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //loginRequired != null 说明运行有这注解的方法是需要登录才能运行的
            if (loginRequired != null && hostHolder.getUser() == null) {
                //方法要登录才能执行的，但当前没有用户登录。要重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
