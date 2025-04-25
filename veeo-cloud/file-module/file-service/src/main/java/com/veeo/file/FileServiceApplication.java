package com.veeo.file;


import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.authority.BaseAuthority;
import com.veeo.sys.api.SettingClient;
import com.veeo.user.api.UserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(clients = {SettingClient.class, UserClient.class})
@MapperScan(basePackages = "com.veeo.file.mapper")
@ComponentScan(basePackages ={"com.veeo.common", "com.veeo.file"})
public class FileServiceApplication implements CommandLineRunner {

    @Autowired
    private AuthorityUtils authorityUtils;

    @Autowired
    private BaseAuthority baseAuthority;

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class);
    }
    @Override
    public void run(String... args) {
        authorityUtils.setGlobalVerify(true, baseAuthority);
    }
}