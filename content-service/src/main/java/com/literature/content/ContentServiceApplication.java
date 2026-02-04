package com.literature.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.literature.content.mapper")
@SpringBootApplication(scanBasePackages = "com.literature")
public class ContentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ContentServiceApplication.class, args);
  }
}
