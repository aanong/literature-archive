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
 * Google Gemini Chat 模型提供商
 * <p>
 * 通过 Gemini 的 OpenAI 兼容端点调用，复用 {@link OpenAiChatModel}。
 * 端点: https://generativelanguage.googleapis.com/v1beta/openai/
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiChatProvider implements ChatModelProvider {

    /**
     * Gemini OpenAI 兼容端点
     */
    private static final String GEMINI_OPENAI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai/";

    private final AiModelProperties properties;

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public ChatLanguageModel buildChatModel() {
        AiModelProperties.GeminiConfig config = properties.getGemini();
        log.info("构建 Gemini Chat 模型（OpenAI 兼容模式）: model={}", config.getChatModel());

        return OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(GEMINI_OPENAI_BASE_URL)
                .modelName(config.getChatModel())
                .temperature(config.getTemperature())
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
}
