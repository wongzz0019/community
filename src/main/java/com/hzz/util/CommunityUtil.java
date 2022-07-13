package com.hzz.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MD5加密
 * @author Bosco
 * @date 2022/2/22
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        //replace替换
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    //hello -> abc123def456
    //hello + 3e4a8(加盐) -> abc123def456abc
    public static String md5(String key){
        //判断传入参数是否空，StringUtils是commons.lang3包下的，需要导入commons.lang3包
        if (StringUtils.isBlank(key)){
            return null;
        }
        //SPRING自带的工具DigestUtils，key为string类型转换byte类型
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String,Object> map){
        //参数封装到JSON对象
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            //遍历key
            for (String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        //输出json字符串
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 25);
        System.out.println(getJSONString(0, "ok", map));
    }
}
