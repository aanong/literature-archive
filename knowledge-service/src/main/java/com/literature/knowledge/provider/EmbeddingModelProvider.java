package com.literature.knowledge.provider;

import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * Embedding 模型提供商策略接口
 * <p>
 * 不同的 AI 提供商实现此接口，提供向量嵌入模型的构建能力。
 * </p>
 */
public interface EmbeddingModelProvider {

    /**
     * 提供商唯一标识
     */
    String getProviderName();

    /**
     * 构建 Embedding 模型实例
     *
     * @return LangChain4j EmbeddingModel 实例
     */
    EmbeddingModel buildEmbeddingModel();
}
