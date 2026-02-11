package com.literature.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Knowledge Service 启动类
 * AI科普知识服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.literature.knowledge.mapper")
public class KnowledgeServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}
