package com.literature.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Knowledge Service 启动类
 * AI科普知识服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.literature.knowledge.mapper")
@EnableFeignClients(basePackages = "com.literature.common.core.feign")
public class KnowledgeServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}
