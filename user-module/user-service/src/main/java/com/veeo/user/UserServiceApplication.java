package com.veeo.user;


import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.file.client.AuditClient;
import com.veeo.file.client.FileClient;
import com.veeo.interest.client.FeedClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.client.FollowClient;
import com.veeo.user.client.UserSubscribeClient;
import com.veeo.video.client.FavoritesClient;
import com.veeo.video.client.TypeClient;
import com.veeo.video.client.VideoClient;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableFeignClients(clients = {
        TypeClient.class, FollowClient.class, FileClient.class,
        InterestClient.class, FavoritesClient.class, AuditClient.class,
        UserSubscribeClient.class, FeedClient.class, VideoClient.class})
@MapperScan(basePackages = "com.veeo.user.mapper")
@ComponentScan(basePackages = {"com.veeo.common", "com.veeo.user"}) // 扫描第三方的包后还有扫描本身的包
public class UserServiceApplication implements CommandLineRunner {

    @Resource
    private AuthorityUtils authorityUtils;

    @Resource
    private BaseAuthority baseAuthority;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class);
    }

    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}