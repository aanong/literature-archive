package com.literature.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class AssetServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AssetServiceApplication.class, args);
  }
}
