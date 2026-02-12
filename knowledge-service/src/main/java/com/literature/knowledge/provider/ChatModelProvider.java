package com.literature.knowledge.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Chat 模型提供商策略接口
 * <p>
 * 不同的 AI 提供商（Ollama、OpenAI、Gemini）实现此接口，
 * 通过 {@link ModelProviderFactory} 根据配置自动选择对应实现。
 * </p>
 */
public interface ChatModelProvider {

    /**
     * 提供商唯一标识（如 "ollama"、"openai"、"gemini"）
     */
    String getProviderName();

    /**
     * 构建 Chat 模型实例
     *
     * @return LangChain4j ChatLanguageModel 实例
     */
    ChatLanguageModel buildChatModel();
}
