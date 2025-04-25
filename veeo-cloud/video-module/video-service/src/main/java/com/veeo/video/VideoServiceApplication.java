package com.veeo.video;

import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.file.api.FileClient;
import com.veeo.file.api.QiNiuFileClient;
import com.veeo.interest.api.FeedClient;
import com.veeo.interest.api.InterestClient;
import com.veeo.user.api.FollowClient;
import com.veeo.sys.api.SettingClient;
import com.veeo.user.api.UserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.veeo.video.mapper")
@EnableFeignClients(clients = {SettingClient.class, FeedClient.class, InterestClient.class,
        QiNiuFileClient.class, FollowClient.class, UserClient.class, FileClient.class})
@ComponentScan(basePackages ={"com.veeo.common", "com.veeo.video"})
public class VideoServiceApplication implements CommandLineRunner  {

    @Autowired
    private AuthorityUtils authorityUtils;

    @Autowired
    private BaseAuthority baseAuthority;

    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class);
    }

    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}