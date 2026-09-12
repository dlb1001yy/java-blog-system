package com.dlbyy.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dlbyy.blog.mapper")
@EnableScheduling
public class JavaBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaBlogApplication.class, args);
    }
}