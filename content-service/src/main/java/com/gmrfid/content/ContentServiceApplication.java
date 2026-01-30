package com.gmrfid.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gmrfid")
public class ContentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ContentServiceApplication.class, args);
  }
}
