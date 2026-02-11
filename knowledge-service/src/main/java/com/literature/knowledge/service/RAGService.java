package com.literature.knowledge.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG服务 (检索增强生成)
 * 结合向量检索和LLM生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final EmbeddingService embeddingService;
    private final LangChainService langChainService;

    private static final String RAG_PROMPT_TEMPLATE = """
            参考资料:
            %s
            
            用户问题: %s
            
            请基于以上参考资料回答问题。如果参考资料中没有相关信息,请如实回答不知晓。
            请确保回答准确、客观,并尽量引用原文。
            """;

    /**
     * RAG问答
     * @param question 用户提问
     * @return AI回答
     */
    public String generateAnswer(String question) {
        // 1. 检索相关知识
        List<EmbeddingMatch<TextSegment>> matches = embeddingService.search(question, 5, 0.7);
        
        String context;
        if (matches.isEmpty()) {
            log.info("未检索到相关知识,将直接使用模型回答");
            context = "暂无相关参考资料";
        } else {
            context = matches.stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.joining("\n\n"));
        }

        // 2. 构建提示词
        String prompt = String.format(RAG_PROMPT_TEMPLATE, context, question);

        // 3. 调用LLM生成
        return langChainService.chat(prompt);
    }
    
    /**
     * 仅检索知识
     */
    public List<String> retrieveKnowledge(String question, int topK) {
        return embeddingService.search(question, topK, 0.6)
                .stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
    }
}
