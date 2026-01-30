package com.gmrfid.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gmrfid")
public class AssetServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AssetServiceApplication.class, args);
  }
}
