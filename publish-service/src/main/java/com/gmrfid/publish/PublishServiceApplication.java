package com.gmrfid.publish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gmrfid")
public class PublishServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PublishServiceApplication.class, args);
  }
}
