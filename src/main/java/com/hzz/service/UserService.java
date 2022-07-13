package com.hzz.service;

import com.hzz.dao.LoginTicketMapper;
import com.hzz.dao.UserMapper;
import com.hzz.entity.LoginTicket;
import com.hzz.entity.User;
import com.hzz.util.CommunityConstant;
import com.hzz.util.CommunityUtil;
import com.hzz.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Bosco
 * @date 2022/2/21
 */

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    //thymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    //域名，从yaml获取，不是Bean
    @Value("${community.path.domain}")
    private String domain;

    //项目名,从yaml获取，
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //通过id查找用户
    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    //注册业务
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        //加盐
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //密码，md5加盐
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        //激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置用户初始头像，随机获取200内
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(200)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        //thymeleaf模板引擎的Context
        Context context = new Context();
        //设置邮箱模板内的内容
        context.setVariable("email", user.getEmail());
        //激活链接： http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //邮件模板位置，填入的参数
        String content = templateEngine.process("/mail/activation", context);
        //发邮箱
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        //map返回null，表示没问题
        return map;
    }

    //账号激活状态，用到我们自定义的常量，要实现所写常量的接口
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            //判断激活码是否一样。是就成功激活
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }


    //登录业务，账号密码，多少时间过期
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        //成功登录
        // 实例化LoginTicket实体类，设置参数，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        //设置随机的登录凭证码
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        //当前时间毫秒数+过期时间毫秒数
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        //存入登录凭证码
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //退出
    public void logout(String ticket) {
        //调用dao层修改登录状态
        loginTicketMapper.updateStatus(ticket, 1);
    }

    //查找登录凭证
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    //更新头像
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    //修改密码
    public Map<String,Object> updatePassword(User user, String oldPassword, String newPassword, String checkPassword){
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPassword","原始密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPassword","新的密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(checkPassword)){
            map.put("checkPassword","确认密码不能为空");
            return map;
        }
        if (!newPassword.equals(checkPassword)){
            map.put("checkPassword","两次输入的密码不一致!");
            return map;
        }
        String oldPwd = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPwd.equals(user.getPassword())){
            map.put("oldPassword","原始密码错误");
            return map;
        }

        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        return map;
    }

}
