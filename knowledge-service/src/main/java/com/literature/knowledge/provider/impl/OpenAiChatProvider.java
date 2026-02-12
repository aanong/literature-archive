package com.literature.knowledge.provider.impl;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.ChatModelProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OpenAI（ChatGPT）Chat 模型提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiChatProvider implements ChatModelProvider {

    private final AiModelProperties properties;

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public ChatLanguageModel buildChatModel() {
        AiModelProperties.OpenAiConfig config = properties.getOpenai();
        log.info("构建 OpenAI Chat 模型: model={}", config.getChatModel());

        return OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getChatModel())
                .temperature(config.getTemperature())
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
}
