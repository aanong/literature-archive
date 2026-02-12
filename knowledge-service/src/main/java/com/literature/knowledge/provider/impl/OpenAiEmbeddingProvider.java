package com.literature.knowledge.provider.impl;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.EmbeddingModelProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OpenAI Embedding 模型提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingProvider implements EmbeddingModelProvider {

    private final AiModelProperties properties;

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public EmbeddingModel buildEmbeddingModel() {
        AiModelProperties.OpenAiConfig config = properties.getOpenai();
        log.info("构建 OpenAI Embedding 模型: model={}", config.getEmbeddingModel());

        return OpenAiEmbeddingModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getEmbeddingModel())
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
}
