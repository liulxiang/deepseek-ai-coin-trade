package com.noodle.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.noodle"})
@EnableJpaRepositories(basePackages = {"com.noodle.app.trade.repository"})
@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("io.netty.leakDetectionLevel", "SIMPLE");
        // 启动Spring Boot应用
        SpringApplication app = new SpringApplication(MainApplication.class);
        // 设置默认配置文件
        app.run(args);
    }
}

