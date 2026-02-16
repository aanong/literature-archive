package com.literature.knowledge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类
 * 
 * 注意: 由于LangChain4j版本兼容性问题,暂时采用简化配置
 * 具体的模型初始化将在Service层按需创建
 */
@Slf4j
@Configuration
public class LangChain4jConfig {
    
    @Value("${langchain4j.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name:deepseek-r1:7b}")
    private String chatModelName;

    @Value("${langchain4j.ollama.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.ollama.embedding-model.model-name:bge-m3}")
    private String embeddingModelName;

    @Value("${langchain4j.milvus.host:localhost}")
    private String milvusHost;

    @Value("${langchain4j.milvus.port:19530}")
    private Integer milvusPort;

    @Value("${langchain4j.milvus.collection-name:literature_segment}")
    private String collectionName;

    @Value("${langchain4j.milvus.dimension:1024}")
    private Integer dimension;
    
    // Getter方法供Service层使用
    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }
    
    public String getChatModelName() {
        return chatModelName;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public String getEmbeddingModelName() {
        return embeddingModelName;
    }
    
    public String getMilvusHost() {
        return milvusHost;
    }
    
    public Integer getMilvusPort() {
        return milvusPort;
    }
    
    public String getCollectionName() {
        return collectionName;
    }
    
    public Integer getDimension() {
        return dimension;
    }
}
