package com.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@MapperScan("com.news.dao")
@EnableTransactionManagement
@EnableScheduling
public class NewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class, args);
        System.out.println("============================================");
        System.out.println("    新闻发布管理系统启动成功!");
        System.out.println("    访问地址: http://localhost:8080");
        System.out.println("============================================");
    }
}
