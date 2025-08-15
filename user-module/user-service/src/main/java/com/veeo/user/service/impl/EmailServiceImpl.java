package com.veeo.user.service.impl;


import com.veeo.user.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * @description:
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final String WARN_MESSAGE = "\nVeeo平台仅供测试学习演示使用, 请勿上传非法内容.";

    @Resource
    private SimpleMailMessage simpleMailMessage;

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromName;


    @Override
    @Async
    public void send(String email, String context) {
        simpleMailMessage.setSubject("Veeo Video");
        simpleMailMessage.setFrom(fromName);
        simpleMailMessage.setTo(email);
        context += WARN_MESSAGE;
        simpleMailMessage.setText(context);
        javaMailSender.send(simpleMailMessage);
    }
}
