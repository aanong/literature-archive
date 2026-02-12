package com.literature.knowledge.service;

import com.literature.knowledge.config.AiModelProperties;
import com.literature.knowledge.provider.ModelProviderFactory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * LangChain AI 服务
 * <p>
 * 通过 {@link ModelProviderFactory} 获取当前配置的 Chat 模型，
 * 支持在 Ollama、OpenAI、Gemini 之间切换。
 * </p>
 */
@Slf4j
@Service
public class LangChainService {

    private final ModelProviderFactory providerFactory;
    private final AiModelProperties properties;
    private ChatLanguageModel chatModel;

    public LangChainService(ModelProviderFactory providerFactory, AiModelProperties properties) {
        this.providerFactory = providerFactory;
        this.properties = properties;
        initChatModel();
    }

    /**
     * 初始化聊天模型（通过 Provider 工厂）
     */
    private void initChatModel() {
        try {
            String provider = properties.getChat().getProvider();
            log.info("正在初始化 Chat 模型, 提供商: {}", provider);
            this.chatModel = providerFactory.getChatModel();
            log.info("Chat 模型初始化成功, 提供商: {}", provider);
        } catch (Exception e) {
            log.error("Chat 模型初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 简单的问答
     */
    public String chat(String userMessage) {
        if (chatModel == null) {
            initChatModel();
            if (chatModel == null) {
                return "AI服务暂时不可用,请检查模型连接";
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
     * 获取当前活动的 Chat 提供商名称
     */
    public String getActiveProviderName() {
        return providerFactory.getActiveChatProviderName();
    }

    /**
     * 重新初始化模型（当配置变更或连接恢复时）
     */
    public void refreshModel() {
        initChatModel();
    }
}
