package com.literature.publish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class PublishServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PublishServiceApplication.class, args);
  }
}
