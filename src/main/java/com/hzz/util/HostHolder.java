package com.hzz.util;

import com.hzz.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    //存
    public void setUser(User user) {
        users.set(user);
    }

    //取
    public User getUser() {
        return users.get();
    }

    //清
    public void clear() {
        users.remove();
    }

}
