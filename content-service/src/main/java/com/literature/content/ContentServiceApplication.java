package com.literature.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class ContentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ContentServiceApplication.class, args);
  }
}
