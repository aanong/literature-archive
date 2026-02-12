package com.literature.knowledge.provider.impl;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.ChatModelProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Ollama Chat 模型提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaChatProvider implements ChatModelProvider {

    private final AiModelProperties properties;

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public ChatLanguageModel buildChatModel() {
        AiModelProperties.OllamaConfig config = properties.getOllama();
        log.info("构建 Ollama Chat 模型: baseUrl={}, model={}", config.getBaseUrl(), config.getChatModel());

        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getChatModel())
                .temperature(config.getTemperature())
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
}
