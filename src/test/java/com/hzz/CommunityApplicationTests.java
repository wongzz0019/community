package com.hzz;

import com.hzz.dao.DiscussPostMapper;
import com.hzz.dao.LoginTicketMapper;
import com.hzz.dao.UserMapper;
import com.hzz.entity.DiscussPost;
import com.hzz.entity.LoginTicket;
import com.hzz.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    void contextLoads() {
    }

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(2);
        System.out.println(user);

        User user1 = userMapper.selectByName("小明");
        System.out.println(user1);

        User email = userMapper.selectByEmail("123456@sina.com");
        System.out.println(email);
    }

    @Test
    void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/111.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    void testUpdateUser(){
        int rows = userMapper.updateHeader(103, "http://www.nowcoder.com/10.png");
        System.out.println(rows);

        int i = userMapper.updatePassword(103, "123123");
        System.out.println(i);

        int a = userMapper.updateStatus(103, 1);
        System.out.println(a);
    }

    @Test
    void testSelectDiscussPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(1, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(1);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginticket);
    }

    @Test
    public void testUpdataLoginTicket(){
        loginTicketMapper.updateStatus("abc",1);
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }


}
