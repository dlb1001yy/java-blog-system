package com.dlbyy.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dlbyy.blog.mapper")
public class JavaBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaBlogApplication.class, args);
    }
}