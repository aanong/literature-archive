package com.literature.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.literature")
public class SearchServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(SearchServiceApplication.class, args);
  }
}
