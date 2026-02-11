package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.entity.QAMessage;
import com.literature.knowledge.entity.QASession;
import com.literature.knowledge.mapper.QAMessageMapper;
import com.literature.knowledge.mapper.QASessionMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QAService {
    
    private final QASessionMapper sessionMapper;
    private final QAMessageMapper messageMapper;
    private final LangChainService langChainService;
    private final RAGService ragService;
    
    private static final String SYSTEM_PROMPT = """
            你是一位博学多才的中国典籍专家,精通经史子集。
            请用通俗易懂的语言回答用户关于中国古籍的问题。
            如果问题涉及具体的历史事实或典籍原文,请尽量准确引用。
            如果不知道答案,请如实回答,不要编造。
            """;
    
    /**
     * 创建新会话
     */
    @Transactional
    public QASession createSession(Long userId, String title) {
        QASession session = new QASession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新会话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setLastMessageAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }
    
    /**
     * 获取用户所有会话
     */
    public List<QASession> getUserSessions(Long userId) {
        return sessionMapper.selectList(
            new LambdaQueryWrapper<QASession>()
                .eq(QASession::getUserId, userId)
                .orderByDesc(QASession::getLastMessageAt)
        );
    }
    
    /**
     * 获取会话消息历史
     */
    public List<QAMessage> getSessionMessages(Long sessionId) {
        return messageMapper.selectList(
            new LambdaQueryWrapper<QAMessage>()
                .eq(QAMessage::getSessionId, sessionId)
                .orderByAsc(QAMessage::getCreatedAt)
        );
    }
    
    /**
     * 提问并获取回答
     */
    @Transactional
    public String ask(Long sessionId, String question) {
        QASession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }
        
        // 1. 保存用户提问
        QAMessage userMsg = new QAMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole(QAMessage.Role.USER);
        userMsg.setContent(question);
        messageMapper.insert(userMsg);
        
        // 2. RAG检索增强
        List<String> relevantKnowledge = ragService.retrieveKnowledge(question, 3);
        String augmentedQuestion = question;
        if (!relevantKnowledge.isEmpty()) {
            String context = String.join("\n\n", relevantKnowledge);
            augmentedQuestion = String.format("基于以下参考资料回答问题:\n%s\n\n问题: %s", context, question);
        }

        // 3. 构建对话历史上下文
        List<ChatMessage> history = buildContextWithAugmentedLastMessage(sessionId, augmentedQuestion);
        
        // 4. 调用AI生成回复
        String answer = langChainService.chat(history);
        
        // 5. 保存AI回复
        QAMessage aiMsg = new QAMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole(QAMessage.Role.ASSISTANT);
        aiMsg.setContent(answer);
        aiMsg.setModelName("deepseek-r1:7b"); // 应该从配置读取
        messageMapper.insert(aiMsg);
        
        // 6. 更新会话状态
        session.setLastMessageAt(LocalDateTime.now());
        session.setMessageCount(session.getMessageCount() + 2);
        sessionMapper.updateById(session);
        
        return answer;
    }
    
    /**
     * 构建上下文,并使用增强后的内容作为最后一条用户消息
     */
    private List<ChatMessage> buildContextWithAugmentedLastMessage(Long sessionId, String augmentedLastMessage) {
        List<ChatMessage> context = new ArrayList<>();
        
        // 添加系统提示词
        context.add(new SystemMessage(SYSTEM_PROMPT));
        
        // 获取所有历史消息 (排除刚保存的那条,因为它需要用augmented版本)
        // 这里简化处理,我们重新获取前N-1条
        // MyBatis-Plus limit syntax: .last("LIMIT 10")
        List<QAMessage> messages = messageMapper.selectList(
            new LambdaQueryWrapper<QAMessage>()
                .eq(QAMessage::getSessionId, sessionId)
                .orderByDesc(QAMessage::getCreatedAt)
                .last("LIMIT 10")
        );
        
        // 倒序处理历史消息
        // messages.get(0) 是刚保存的最新用户消息
        // 我们跳过它,手动添加augmented version
        
        if (messages.isEmpty()) {
             context.add(new UserMessage(augmentedLastMessage));
             return context;
        }

        // i从1开始,跳过最新的(即下标0)
        // 注意: messages是按时间倒序排列的
        for (int i = messages.size() - 1; i >= 1; i--) {
            QAMessage msg = messages.get(i);
            if (msg.getRole() == QAMessage.Role.USER) {
                context.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == QAMessage.Role.ASSISTANT) {
                context.add(new AiMessage(msg.getContent()));
            }
        }
        
        // 添加最新的增强版用户消息
        context.add(new UserMessage(augmentedLastMessage));
        
        return context;
    }
}
