package com.literature.knowledge.provider.impl;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.EmbeddingModelProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Ollama Embedding 模型提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaEmbeddingProvider implements EmbeddingModelProvider {

    private final AiModelProperties properties;

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public EmbeddingModel buildEmbeddingModel() {
        AiModelProperties.OllamaConfig config = properties.getOllama();
        log.info("构建 Ollama Embedding 模型: baseUrl={}, model={}", config.getBaseUrl(), config.getEmbeddingModel());

        return OllamaEmbeddingModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getEmbeddingModel())
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
}
