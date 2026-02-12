package com.literature.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模型统一配置
 * <p>
 * 通过 {@code ai-model} 前缀管理所有模型提供商的配置。
 * 支持按 Chat / Embedding 分别指定提供商。
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai-model")
public class AiModelProperties {

    /**
     * Chat 模型配置
     */
    private ChatConfig chat = new ChatConfig();

    /**
     * Embedding 模型配置
     */
    private EmbeddingConfig embedding = new EmbeddingConfig();

    /**
     * Ollama 提供商配置
     */
    private OllamaConfig ollama = new OllamaConfig();

    /**
     * OpenAI 提供商配置
     */
    private OpenAiConfig openai = new OpenAiConfig();

    /**
     * Gemini 提供商配置
     */
    private GeminiConfig gemini = new GeminiConfig();

    @Data
    public static class ChatConfig {
        /**
         * 当前使用的 Chat 模型提供商: ollama / openai / gemini
         */
        private String provider = "ollama";
    }

    @Data
    public static class EmbeddingConfig {
        /**
         * 当前使用的 Embedding 模型提供商: ollama / openai
         */
        private String provider = "ollama";
    }

    @Data
    public static class OllamaConfig {
        private String baseUrl = "http://localhost:11434";
        private String chatModel = "deepseek-r1:7b";
        private String embeddingModel = "nomic-embed-text";
        private Double temperature = 0.7;
        private Integer timeout = 60;
    }

    @Data
    public static class OpenAiConfig {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1";
        private String chatModel = "gpt-4o";
        private String embeddingModel = "text-embedding-3-small";
        private Double temperature = 0.7;
        private Integer timeout = 60;
    }

    @Data
    public static class GeminiConfig {
        private String apiKey = "";
        private String chatModel = "gemini-2.0-flash";
        private Double temperature = 0.7;
        private Integer timeout = 60;
    }
}
