package com.literature.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class ChatServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ChatServiceApplication.class, args);
  }
}
