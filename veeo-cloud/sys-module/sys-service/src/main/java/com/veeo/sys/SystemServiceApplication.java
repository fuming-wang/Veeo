package com.veeo.sys;


import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.user.api.UserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.veeo.sys.mapper"})
@ComponentScan(basePackages = {"com.veeo.common", "com.veeo.sys"})
@EnableFeignClients(clients = {UserClient.class})
public class SystemServiceApplication implements CommandLineRunner {


    @Autowired
    private AuthorityUtils authorityUtils;

    @Autowired
    private BaseAuthority baseAuthority;
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class);
    }

    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}