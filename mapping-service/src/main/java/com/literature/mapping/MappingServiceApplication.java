package com.literature.mapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class MappingServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(MappingServiceApplication.class, args);
  }
}
