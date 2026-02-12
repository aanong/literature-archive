package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.entity.QAMessage;
import com.literature.knowledge.entity.QASession;
import com.literature.knowledge.mapper.QAMessageMapper;
import com.literature.knowledge.mapper.QASessionMapper;
import dev.langchain4j.data.message.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QAServiceTest {

    @Mock
    private QASessionMapper sessionMapper;

    @Mock
    private QAMessageMapper messageMapper;

    @Mock
    private LangChainService langChainService;

    @Mock
    private RAGService ragService;

    @Mock
    private HistoryGuardService historyGuardService;

    @InjectMocks
    private QAService qaService;

    private Long sessionId = 1L;
    private Long userId = 100L;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createSession_ShouldReturnSession() {
        when(sessionMapper.insert(any(QASession.class))).thenReturn(1);

        QASession session = qaService.createSession(userId, "Test Session");

        assertEquals("Test Session", session.getTitle());
        assertEquals(userId, session.getUserId());
        verify(sessionMapper).insert(any(QASession.class));
    }

    @Test
    void ask_ShouldUseRAGAndReturnAnswer() {
        QASession session = new QASession();
        session.setId(sessionId);
        session.setMessageCount(0);

        when(sessionMapper.selectById(sessionId)).thenReturn(session);
        when(messageMapper.insert(any(QAMessage.class))).thenReturn(1);
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        when(ragService.retrieveKnowledge("RAG Question", 3)).thenReturn(Collections.singletonList("Context info"));
        when(langChainService.chat(anyList())).thenReturn("AI Answer");

        when(sessionMapper.updateById(any(QASession.class))).thenReturn(1);

        // Stub guard to pass through the answer
        when(historyGuardService.guardAnswer(anyString(), anyString(), anyList()))
                .thenAnswer(inv -> new HistoryGuardService.GuardedResponse(
                        inv.getArgument(0), List.of(), List.of(), List.of()));

        String answer = qaService.ask(sessionId, "RAG Question", null);

        assertEquals("AI Answer", answer);

        // Verify RAG interaction
        verify(ragService).retrieveKnowledge("RAG Question", 3);

        // Verify Message Saving: User Msg + AI Msg
        verify(messageMapper, times(2)).insert(any(QAMessage.class));

        // Verify Session Update
        verify(sessionMapper).updateById(session);
    }
}
