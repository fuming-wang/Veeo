package com.veeo.user;


import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.file.api.FileClient;
import com.veeo.interest.api.FeedClient;
import com.veeo.interest.api.InterestClient;
import com.veeo.sys.api.CaptchaClient;
import com.veeo.user.api.FollowClient;
import com.veeo.user.api.UserSubscribeClient;
import com.veeo.video.api.*;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableFeignClients(clients = {
        CaptchaClient.class, TypeClient.class, FollowClient.class, FileClient.class,
        InterestClient.class, FavoritesClient.class, TextAuditClient.class, ImageAuditClient.class,
        UserSubscribeClient.class, FeedClient.class, VideoClient.class})
@MapperScan(basePackages = "com.veeo.user.mapper")
@ComponentScan(basePackages = {"com.veeo.common", "com.veeo.user"}) // 扫描第三方的包后还有扫描本身的包
public class UserServiceApplication implements CommandLineRunner {

    @Autowired
    private AuthorityUtils authorityUtils;

    @Autowired
    private BaseAuthority baseAuthority;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class);
    }

    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}