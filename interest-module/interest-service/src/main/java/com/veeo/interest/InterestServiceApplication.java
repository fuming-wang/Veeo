package com.veeo.interest;


import com.veeo.video.client.TypeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@EnableFeignClients(clients = {TypeClient.class})
@ComponentScan(basePackages = {"com.veeo.common", "com.veeo.interest"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class InterestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterestServiceApplication.class);
    }
}