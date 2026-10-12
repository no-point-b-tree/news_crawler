package com.archer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@MapperScan("com.archer.**.mapper")
public class App {

    @Value("${spring.profiles.active}")
    private String active;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostConstruct
    public void init() {
        LatestSpiderTaskServiceImpl instance = LatestSpiderTaskServiceImpl.getInstance(active);
        instance.release();
        log.info("初始化成功");
    }

}
