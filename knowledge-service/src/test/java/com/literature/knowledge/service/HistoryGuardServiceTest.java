package com.literature.knowledge.service;

import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.entity.KnowledgeItem;
import com.literature.knowledge.mapper.HistoricalTimelineMapper;
import com.literature.knowledge.mapper.SourceConflictMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HistoryGuardServiceTest {

    @Mock
    private HistoricalTimelineMapper timelineMapper;

    @Mock
    private SourceConflictMapper conflictMapper;

    private HistoryGuardProperties properties;
    private FactVerificationService factVerificationService;
    private TimelineService timelineService;
    private ConflictResolutionService conflictResolutionService;
    private HistoryGuardService guardService;

    @BeforeEach
    void setUp() {
        properties = new HistoryGuardProperties();
        factVerificationService = new FactVerificationService(properties);
        timelineService = new TimelineService(timelineMapper, properties);
        conflictResolutionService = new ConflictResolutionService(conflictMapper, properties);
        guardService = new HistoryGuardService(properties, factVerificationService, timelineService,
                conflictResolutionService);
    }

    @Test
    void guardAnswer_WhenDisabled_ShouldPassThrough() {
        properties.setEnabled(false);
        String rawAnswer = "众所周知，曹操号称百万大军。";

        HistoryGuardService.GuardedResponse result = guardService.guardAnswer(rawAnswer, "赤壁之战", List.of());

        assertEquals(rawAnswer, result.answer()); // untouched
        assertTrue(result.factWarnings().isEmpty());
    }

    @Test
    void guardAnswer_WhenEnabled_ShouldFilterForbiddenPhrases() {
        properties.setEnabled(true);
        String rawAnswer = "众所周知，秦始皇于公元前221年统一六国。";

        HistoryGuardService.GuardedResponse result = guardService.guardAnswer(rawAnswer, "秦统一", List.of());

        assertFalse(result.answer().contains("众所周知"));
        assertTrue(result.answer().contains("据史料记载"));
        assertFalse(result.factWarnings().isEmpty());
    }

    @Test
    void buildGuardedSystemPrompt_ShouldContainAllSections() {
        properties.setEnabled(true);
        String original = "你是一位历史专家。";

        String guarded = guardService.buildGuardedSystemPrompt(original);

        assertTrue(guarded.contains("你是一位历史专家。"));
        assertTrue(guarded.contains("防幻觉规则"));
        assertTrue(guarded.contains("时间标注规则"));
        assertTrue(guarded.contains("多源处理规则"));
        assertTrue(guarded.contains("众所周知")); // listed as forbidden
    }

    @Test
    void buildGuardedSystemPrompt_WhenDisabled_ShouldReturnOriginal() {
        properties.setEnabled(false);
        String original = "你是一位历史专家。";

        String guarded = guardService.buildGuardedSystemPrompt(original);

        assertEquals(original, guarded);
    }

    @Test
    void guardAnswer_StrictMode_ShouldAddDisclaimer() {
        properties.setEnabled(true);
        properties.getAntiHallucination().setLevel(
                HistoryGuardProperties.AntiHallucination.Level.STRICT);
        properties.getAntiHallucination().setMaxUnsourcedClaims(0);
        String rawAnswer = "众所周知，曹操率军南下荆州。毫无疑问这是改变三国格局的战役。";

        HistoryGuardService.GuardedResponse result = guardService.guardAnswer(rawAnswer, "赤壁", List.of());

        assertTrue(result.answer().contains("严格模式审核提醒"));
        assertFalse(result.factWarnings().isEmpty());
    }
}
