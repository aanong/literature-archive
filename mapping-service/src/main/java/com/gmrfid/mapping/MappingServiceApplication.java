package com.gmrfid.mapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gmrfid")
public class MappingServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(MappingServiceApplication.class, args);
  }
}
