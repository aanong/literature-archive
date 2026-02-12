package com.literature.knowledge.service;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.ModelProviderFactory;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量嵌入服务
 * <p>
 * 通过 {@link ModelProviderFactory} 获取当前配置的 Embedding 模型，
 * 支持在 Ollama、OpenAI 之间切换。Milvus 向量存储保持不变。
 * </p>
 */
@Slf4j
@Service
public class EmbeddingService {

    private final ModelProviderFactory providerFactory;
    private final AiModelProperties properties;

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;

    // Milvus 配置仍从原有 langchain4j 配置读取
    @Value("${langchain4j.milvus.host}")
    private String milvusHost;

    @Value("${langchain4j.milvus.port}")
    private Integer milvusPort;

    @Value("${langchain4j.milvus.collection-name}")
    private String collectionName;

    @Value("${langchain4j.milvus.dimension}")
    private Integer dimension;

    public EmbeddingService(ModelProviderFactory providerFactory, AiModelProperties properties) {
        this.providerFactory = providerFactory;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        initEmbeddingModel();
        initEmbeddingStore();
    }

    /**
     * 初始化 Embedding 模型（通过 Provider 工厂）
     */
    private void initEmbeddingModel() {
        try {
            String provider = properties.getEmbedding().getProvider();
            log.info("正在初始化 Embedding 模型, 提供商: {}", provider);
            this.embeddingModel = providerFactory.getEmbeddingModel();
            log.info("Embedding 模型初始化成功, 提供商: {}", provider);
        } catch (Exception e) {
            log.error("Embedding 模型初始化失败: {}", e.getMessage());
        }
    }

    private void initEmbeddingStore() {
        try {
            log.info("正在初始化 Milvus 向量存储: {}:{}/{}", milvusHost, milvusPort, collectionName);
            this.embeddingStore = MilvusEmbeddingStore.builder()
                    .host(milvusHost)
                    .port(milvusPort)
                    .collectionName(collectionName)
                    .dimension(dimension)
                    .build();
            log.info("Milvus 向量存储初始化成功");
        } catch (Exception e) {
            log.error("Milvus 向量存储初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 将文本转换为向量
     */
    public Embedding embed(String text) {
        if (embeddingModel == null) {
            throw new RuntimeException("嵌入模型未初始化");
        }
        return embeddingModel.embed(text).content();
    }

    /**
     * 将文本段及其向量存储到 Milvus
     */
    public String store(TextSegment textSegment) {
        if (embeddingModel == null || embeddingStore == null) {
            throw new RuntimeException("嵌入服务未就绪");
        }
        Embedding embedding = embeddingModel.embed(textSegment).content();
        return embeddingStore.add(embedding, textSegment);
    }

    public void storeAll(List<TextSegment> textSegments) {
        if (embeddingModel == null || embeddingStore == null) {
            throw new RuntimeException("嵌入服务未就绪");
        }
        List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();
        embeddingStore.addAll(embeddings, textSegments);
    }

    /**
     * 在 Milvus 中搜索相似文本段
     */
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults) {
        if (embeddingModel == null || embeddingStore == null) {
            throw new RuntimeException("嵌入服务未就绪");
        }
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, maxResults);
    }

    /**
     * 在 Milvus 中搜索相似文本段（带分数阈值）
     */
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore) {
        if (embeddingModel == null || embeddingStore == null) {
            throw new RuntimeException("嵌入服务未就绪");
        }
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, maxResults, minScore);
    }

    /**
     * 获取当前活动的 Embedding 提供商名称
     */
    public String getActiveProviderName() {
        return providerFactory.getActiveEmbeddingProviderName();
    }
}
