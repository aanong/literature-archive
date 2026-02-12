package com.literature.knowledge.provider;

import com.literature.knowledge.config.AiModelProperties;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模型提供商工厂
 * <p>
 * 根据 {@code ai-model.chat.provider} / {@code ai-model.embedding.provider}
 * 配置自动选择对应的策略实现，构建模型实例。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelProviderFactory {

    private final AiModelProperties properties;
    private final List<ChatModelProvider> chatProviders;
    private final List<EmbeddingModelProvider> embeddingProviders;

    private Map<String, ChatModelProvider> chatProviderMap;
    private Map<String, EmbeddingModelProvider> embeddingProviderMap;

    @PostConstruct
    public void init() {
        // 将所有 Provider 按名称索引
        chatProviderMap = chatProviders.stream()
                .collect(Collectors.toMap(ChatModelProvider::getProviderName, Function.identity()));
        embeddingProviderMap = embeddingProviders.stream()
                .collect(Collectors.toMap(EmbeddingModelProvider::getProviderName, Function.identity()));

        log.info("已注册 Chat Provider: {}", chatProviderMap.keySet());
        log.info("已注册 Embedding Provider: {}", embeddingProviderMap.keySet());
    }

    /**
     * 获取当前配置的 Chat 模型
     */
    public ChatLanguageModel getChatModel() {
        String providerName = properties.getChat().getProvider();
        ChatModelProvider provider = chatProviderMap.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException(
                    String.format("未找到 Chat Provider: '%s', 可用: %s", providerName, chatProviderMap.keySet()));
        }
        return provider.buildChatModel();
    }

    /**
     * 获取当前配置的 Embedding 模型
     */
    public EmbeddingModel getEmbeddingModel() {
        String providerName = properties.getEmbedding().getProvider();
        EmbeddingModelProvider provider = embeddingProviderMap.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException(
                    String.format("未找到 Embedding Provider: '%s', 可用: %s", providerName, embeddingProviderMap.keySet()));
        }
        return provider.buildEmbeddingModel();
    }

    /**
     * 获取当前 Chat 提供商名称
     */
    public String getActiveChatProviderName() {
        return properties.getChat().getProvider();
    }

    /**
     * 获取当前 Embedding 提供商名称
     */
    public String getActiveEmbeddingProviderName() {
        return properties.getEmbedding().getProvider();
    }
}
