package com.literature.knowledge.service;

import com.literature.knowledge.config.LangChain4jConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * LangChain AI服务
 * 封装与Ollama的交互逻辑
 */
@Slf4j
@Service
public class LangChainService {
    
    private final LangChain4jConfig config;
    private ChatLanguageModel chatModel;
    
    public LangChainService(LangChain4jConfig config) {
        this.config = config;
        initChatModel();
    }
    
    /**
     * 初始化聊天模型
     */
    private void initChatModel() {
        try {
            log.info("正在初始化Ollama聊天模型: {}", config.getChatModelName());
            this.chatModel = OllamaChatModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getChatModelName())
                    .temperature(config.getTemperature())
                    .timeout(Duration.ofSeconds(60))
                    .build();
            log.info("Ollama聊天模型初始化成功");
        } catch (Exception e) {
            log.error("Ollama聊天模型初始化失败: {}", e.getMessage());
        }
    }
    
    /**
     * 简单的问答
     */
    public String chat(String userMessage) {
        if (chatModel == null) {
            initChatModel();
            if (chatModel == null) {
                return "AI服务暂时不可用,请检查Ollama连接";
            }
        }
        return chatModel.generate(userMessage);
    }
    
    /**
     * 带系统提示词的问答
     */
    public String chat(String systemPrompt, String userMessage) {
        if (chatModel == null) {
            initChatModel();
            if (chatModel == null) {
                return "AI服务暂时不可用";
            }
        }
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userMessage));
        
        Response<AiMessage> response = chatModel.generate(messages);
        return response.content().text();
    }
    
    /**
     * 多轮对话
     */
    public String chat(List<ChatMessage> history) {
        if (chatModel == null) {
            initChatModel();
            if (chatModel == null) {
                return "AI服务暂时不可用";
            }
        }
        
        Response<AiMessage> response = chatModel.generate(history);
        return response.content().text();
    }
    
    /**
     * 重新初始化模型(当配置变更或连接恢复时)
     */
    public void refreshModel() {
        initChatModel();
    }
}
