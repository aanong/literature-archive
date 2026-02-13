package com.literature.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.literature.content.mapper")
@SpringBootApplication(scanBasePackages = "com.literature")
@EnableFeignClients(basePackages = "com.literature.common.core.feign")
public class ContentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ContentServiceApplication.class, args);
  }
}
