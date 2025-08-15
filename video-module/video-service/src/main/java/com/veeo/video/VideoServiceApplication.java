package com.veeo.video;

import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.file.client.*;
import com.veeo.interest.client.FeedClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.client.FollowClient;
import com.veeo.user.client.UserClient;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.veeo.video.mapper")
@EnableFeignClients(clients = {
        FeedClient.class, InterestClient.class, FileClient.class,
        QiNiuFileClient.class, FollowClient.class, UserClient.class,
        AuditClient.class,})
@ComponentScan(basePackages = {"com.veeo.common", "com.veeo.video"})
public class VideoServiceApplication implements CommandLineRunner {

    @Resource
    private AuthorityUtils authorityUtils;

    @Resource
    private BaseAuthority baseAuthority;

    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class);
    }

    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}