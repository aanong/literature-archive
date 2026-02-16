package com.literature.common.core.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置
 * 
 * 解决 JavaScript Number 精度丢失问题：
 * JS 的 Number 类型使用 IEEE 754 双精度浮点数，最大安全整数为 2^53-1 (9007199254740991)。
 * Java 的 Long 最大值为 2^63-1，超过 JS 安全范围的 Long 值在前端会丢失精度。
 * 
 * 解决方案：将 Long 类型序列化为 String，前端以字符串形式处理 ID。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringSerializer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modules(module);
        };
    }
}
