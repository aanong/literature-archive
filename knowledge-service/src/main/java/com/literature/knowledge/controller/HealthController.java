package com.literature.knowledge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/actuator")
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\"}";
    }
}
