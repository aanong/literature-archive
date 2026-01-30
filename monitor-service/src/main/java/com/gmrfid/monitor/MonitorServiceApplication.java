package com.gmrfid.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gmrfid")
public class MonitorServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(MonitorServiceApplication.class, args);
  }
}
