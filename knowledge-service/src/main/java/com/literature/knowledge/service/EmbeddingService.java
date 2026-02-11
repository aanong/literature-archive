package com.literature.knowledge.service;

import com.literature.knowledge.config.LangChain4jConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 向量嵌入服务
 * 负责与Ollama嵌入模型和Milvus向量数据库交互
 */
@Slf4j
@Service
public class EmbeddingService {

    private final LangChain4jConfig config;
    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;

    public EmbeddingService(LangChain4jConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        initEmbeddingModel();
        initEmbeddingStore();
    }

    private void initEmbeddingModel() {
        try {
            log.info("正在初始化Ollama嵌入模型: {}", config.getEmbeddingModelName());
            this.embeddingModel = OllamaEmbeddingModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getEmbeddingModelName())
                    .timeout(Duration.ofSeconds(60))
                    .build();
            log.info("Ollama嵌入模型初始化成功");
        } catch (Exception e) {
            log.error("Ollama嵌入模型初始化失败: {}", e.getMessage());
        }
    }

    private void initEmbeddingStore() {
        try {
            log.info("正在初始化Milvus向量存储: {}:{}/{}", config.getMilvusHost(), config.getMilvusPort(), config.getCollectionName());
            this.embeddingStore = MilvusEmbeddingStore.builder()
                    .host(config.getMilvusHost())
                    .port(config.getMilvusPort())
                    .collectionName(config.getCollectionName())
                    .dimension(config.getDimension())
                    .build();
            log.info("Milvus向量存储初始化成功");
        } catch (Exception e) {
            log.error("Milvus向量存储初始化失败: {}", e.getMessage());
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
     * 将文本段及其向量存储到Milvus
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
     * 在Milvus中搜索相似文本段
     */
    public List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> search(String query, int maxResults) {
        if (embeddingModel == null || embeddingStore == null) {
             throw new RuntimeException("嵌入服务未就绪");
        }
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, maxResults); // minimumScore default 0.0
    }
    
    /**
     * 在Milvus中搜索相似文本段（带分阈值）
     */
     public List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore) {
        if (embeddingModel == null || embeddingStore == null) {
             throw new RuntimeException("嵌入服务未就绪");
        }
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, maxResults, minScore);
    }
}
