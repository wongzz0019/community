package com.hzz.controller;

import com.hzz.annotation.LoginRequired;
import com.hzz.entity.User;
import com.hzz.service.UserService;
import com.hzz.util.CommunityUtil;
import com.hzz.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //访问账号设置页面
    @LoginRequired
    @GetMapping(path = "/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    //上传头像
    //@LoginRequired 自定义注解，程序运行就有效，需要用户登录才能执行这方法
    @LoginRequired
    @PostMapping(path = "/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        //用MultipartFile类型接收传入的图片
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        //获取原始文件名(包含后缀名)
        String fileName = headerImage.getOriginalFilename();
        //获得后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径，这里先存到本地服务器上
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件，(可以检查uploadPath是否存在, 否则创建该目录)
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();   //得到当前用户
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);// 更新数据库

        return "redirect:/index";
    }

    //获取头像
    @GetMapping(path = "/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 得到服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片类型（image/png、image/jpg）
        response.setContentType("image/" + suffix);
        try (
                //输入流
                FileInputStream fis = new FileInputStream(fileName);
                //输出流
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    //修改密码
    @PostMapping(path = "/updatePassword")
    public String updataPassword(String oldPassword, String newPassword, String checkPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword, checkPassword);
        if (map==null || map.isEmpty()){
            return "redirect:/index";
        }else {
            model.addAttribute("oldPasswordMsg",map.get(oldPassword));
            model.addAttribute("newPasswordMsg",map.get(newPassword));
            model.addAttribute("checkPasswordMsg",map.get(checkPassword));
            return "/site/setting";
        }

    }

}
