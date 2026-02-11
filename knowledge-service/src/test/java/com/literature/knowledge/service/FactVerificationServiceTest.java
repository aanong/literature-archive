package com.literature.knowledge.service;

import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.config.HistoryGuardProperties.AntiHallucination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactVerificationServiceTest {

    private HistoryGuardProperties properties;
    private FactVerificationService service;

    @BeforeEach
    void setUp() {
        properties = new HistoryGuardProperties();
        service = new FactVerificationService(properties);
    }

    @Test
    void filterForbiddenPhrases_StandardMode_ShouldReplacePhrases() {
        properties.getAntiHallucination().setLevel(AntiHallucination.Level.STANDARD);
        String input = "众所周知，秦始皇统一了六国。毫无疑问，这是伟大的历史事件。";
        List<String> warnings = new ArrayList<>();

        String result = service.filterForbiddenPhrases(input, warnings);

        assertFalse(result.contains("众所周知"));
        assertFalse(result.contains("毫无疑问"));
        assertTrue(result.contains("据史料记载"));
        assertEquals(2, warnings.size());
    }

    @Test
    void filterForbiddenPhrases_LenientMode_ShouldKeepPhrases() {
        properties.getAntiHallucination().setLevel(AntiHallucination.Level.LENIENT);
        String input = "众所周知，秦始皇统一了六国。";
        List<String> warnings = new ArrayList<>();

        String result = service.filterForbiddenPhrases(input, warnings);

        assertTrue(result.contains("众所周知")); // LENIENT keeps the phrase
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("宽松模式已保留"));
    }

    @Test
    void verify_StrictMode_ShouldAddDisclaimer() {
        properties.getAntiHallucination().setLevel(AntiHallucination.Level.STRICT);
        properties.getAntiHallucination().setMaxUnsourcedClaims(0);
        String answer = "众所周知，曹操率八十万大军南下。这一说法被历史证明是夸大的。";

        FactVerificationService.VerificationResult result = service.verify(answer, List.of());

        assertFalse(result.passed());
        assertTrue(result.processedAnswer().contains("严格模式审核提醒"));
        assertTrue(result.warnings().size() > 0);
    }

    @Test
    void verify_WithCitations_ShouldPass() {
        properties.getAntiHallucination().setLevel(AntiHallucination.Level.STANDARD);
        properties.getAntiHallucination().setMaxUnsourcedClaims(10);
        // Answer with proper citations should pass
        String answer = "「操军方连船舰，首尾相接」——《三国志·周瑜传》";

        FactVerificationService.VerificationResult result = service.verify(answer, List.of());

        assertTrue(result.passed());
    }
}
