package com.hzz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author Bosco
 * @date 2022/2/22
 */

@Component
public class MailClient {

    private static final Logger logger  = LoggerFactory.getLogger(MailClient.class);

    //核心组件
    @Autowired
    private JavaMailSender mailSender;

    //注入服务端发送方，在yaml中获取
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String target, String subject, String content){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            //创建帮助类
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //设置
            helper.setFrom(from);
            helper.setTo(target);
            helper.setSubject(subject);
            //true 允许支持html文本
            helper.setText(content,true);
            //发送
            mailSender.send(helper.getMimeMessage());
            logger.info("发送邮件成功");
        } catch (MessagingException e){
            logger.error("发送邮件失败:"+ e.getMessage());
        }
    }
}
