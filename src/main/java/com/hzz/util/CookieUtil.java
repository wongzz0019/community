package com.hzz.util;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Bosco
 * @date 2022/2/22
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request, String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("参数为空");
        }

        //获取所有cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                //判断要查的cookie是否在内
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
