package com.veeo.sys.service.impl;


import com.veeo.sys.service.EmailService;
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


    private final SimpleMailMessage simpleMailMessage;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    String fromName;

    public EmailServiceImpl(SimpleMailMessage simpleMailMessage, JavaMailSender javaMailSender) {
        this.simpleMailMessage = simpleMailMessage;
        this.javaMailSender = javaMailSender;
    }


    @Override
    @Async
    public void send(String email, String context) {
        simpleMailMessage.setSubject("幸运日");
        simpleMailMessage.setFrom(fromName);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setText(context);
        javaMailSender.send(simpleMailMessage);
    }
}
