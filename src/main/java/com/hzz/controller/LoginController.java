package com.hzz.controller;

import com.google.code.kaptcha.Producer;
import com.hzz.entity.User;
import com.hzz.service.UserService;
import com.hzz.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Bosco
 * @date 2022/2/22
 */

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //跳转到注册页
    @GetMapping(path = "/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    //跳转到登录页
    @GetMapping(path = "/login")
    public String getLoginPage(){
        return "/site/login";
    }

    //账号注册
    @PostMapping(path = "/register")
    public String register(Model model, User user) {
        //调用业务层，注册账号
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //成功
            model.addAttribute("msg", "注册成功,请前往邮箱激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            //失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //账号激活
    // 激活链接：http://localhost:8080/community/activation/101/code  ;  @PathVariable路径取值
    @GetMapping(path = "/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        //先实现常量接口，再判断
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //生成验证码方法，这里返回的是个方法用void，用HttpServletResponse把验证码图片响应给浏览器
    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码，四位数字符串
        String text = kaptchaProducer.createText();
        //生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            //输出流
            OutputStream os = response.getOutputStream();
            //图片的输出工具
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    //登录。
    @PostMapping(path = "/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response) {
        // 检查验证码 ，通过session获取存入的验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //登录成功，用户信息含有登录凭证ticket
        if (map.containsKey("ticket")) {
            //实例化一个cookie，存cookie生效路径、时间
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            //将cookie发送给页面，在响应时发送给浏览器
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //退出
    @GetMapping(path = "/logout")
    public String logout(@CookieValue("ticket") String ticket){
        //从cookie中获取登录凭证ticket，调用业务层，并修改登录凭证状态
        userService.logout(ticket);
        return "redirect:/login";
    }

}
