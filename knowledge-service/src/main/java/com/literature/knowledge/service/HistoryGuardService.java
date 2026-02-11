package com.literature.knowledge.service;

import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.entity.KnowledgeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史防护统一门面服务
 *
 * 组合 FactVerificationService、TimelineService、ConflictResolutionService
 * 对AI回答进行全链路防护：
 * 事实核验 → 时间线校验 → 冲突标注 → 输出优化后答案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryGuardService {

    private final HistoryGuardProperties properties;
    private final FactVerificationService factVerificationService;
    private final TimelineService timelineService;
    private final ConflictResolutionService conflictResolutionService;

    /**
     * 构建带防幻觉约束的增强 prompt
     * 在原始 system prompt 基础上注入防护规则
     */
    public String buildGuardedSystemPrompt(String originalSystemPrompt) {
        if (!properties.isEnabled()) {
            return originalSystemPrompt;
        }

        StringBuilder sb = new StringBuilder(originalSystemPrompt);
        sb.append("\n\n");

        // 防幻觉规则
        HistoryGuardProperties.AntiHallucination ah = properties.getAntiHallucination();
        sb.append("【防幻觉规则 - 严格等级: ").append(ah.getLevel()).append("】\n");
        if (ah.isRequireSourceCitation()) {
            sb.append("- 每个历史论断必须标注出处，格式：「原文」——《书名·卷名》\n");
            sb.append("- 如无法确认出处，必须标注\"").append(ah.getUncertaintyMarkers().getPrefix()).append("\"\n");
        }
        if (!ah.getForbiddenPhrases().isEmpty()) {
            sb.append("- 禁止使用以下表述：").append(String.join("、", ah.getForbiddenPhrases())).append("\n");
        }

        // 时间规则
        if (properties.getTimeline().isEnabled()) {
            HistoryGuardProperties.Timeline tl = properties.getTimeline();
            sb.append("\n【时间标注规则】\n");
            switch (tl.getCalendarSystem()) {
                case MIXED -> sb.append("- 使用格式：").append(tl.getDateFormat()).append("\n");
                case GREGORIAN -> sb.append("- 统一使用公元纪年\n");
                case CHINESE_ERA -> sb.append("- 使用中国年号纪年，附注朝代\n");
            }
            sb.append("- 涉及年代时，必须明确标注，不得含糊\n");
        }

        // 多源处理规则
        if (properties.getConflict().isEnabled()) {
            sb.append("\n【多源处理规则】\n");
            switch (properties.getConflict().getResolutionStrategy()) {
                case MULTI_VIEW -> {
                    sb.append("- 若不同史书记载相互矛盾，必须列出各方说法\n");
                    sb.append("- 格式：《史记》载\"...\"，而《汉书》载\"...\"\n");
                }
                case SINGLE_AUTHORITY -> sb.append("- 以正史记载为准，无需列出争议观点\n");
                case SCHOLARLY_CONSENSUS -> {
                    sb.append("- 优先引用学术主流观点\n");
                    sb.append("- 如有争议，先给出共识再附注不同看法\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * 对AI原始回答做全链路防护后处理
     *
     * @param rawAnswer    AI原始回答
     * @param question     用户原始问题
     * @param relatedItems 检索到的相关知识条目
     * @return 防护处理后的回答
     */
    public GuardedResponse guardAnswer(String rawAnswer, String question, List<KnowledgeItem> relatedItems) {
        if (!properties.isEnabled()) {
            return new GuardedResponse(rawAnswer, List.of(), List.of(), List.of());
        }

        List<String> allWarnings = new ArrayList<>();
        String processedAnswer = rawAnswer;

        // 1. 事实核验（防幻觉）
        FactVerificationService.VerificationResult verifyResult = factVerificationService.verify(processedAnswer,
                relatedItems);
        processedAnswer = verifyResult.processedAnswer();
        allWarnings.addAll(verifyResult.warnings());

        // 2. 时间线校验
        List<String> timelineViolations = new ArrayList<>();
        if (properties.getTimeline().isEnabled() && relatedItems != null) {
            for (KnowledgeItem item : relatedItems) {
                if (item.getId() != null) {
                    List<String> violations = timelineService.validateChronology(item.getId());
                    timelineViolations.addAll(violations);
                }
            }
        }

        // 3. 冲突标注
        List<String> conflictNotes = new ArrayList<>();
        if (properties.getConflict().isEnabled() && relatedItems != null) {
            for (KnowledgeItem item : relatedItems) {
                if (item.getId() != null) {
                    String conflictText = conflictResolutionService.formatAllConflicts(item.getId());
                    if (!conflictText.isEmpty()) {
                        processedAnswer += conflictText;
                        conflictNotes.add("已附加\"" + item.getTitle() + "\"的史料差异注释");
                    }
                }
            }
        }

        log.info("历史防护处理完成: 事实警告={}, 时间线问题={}, 冲突标注={}",
                allWarnings.size(), timelineViolations.size(), conflictNotes.size());

        return new GuardedResponse(processedAnswer, allWarnings, timelineViolations, conflictNotes);
    }

    /**
     * 防护处理结果
     */
    public record GuardedResponse(
            String answer,
            List<String> factWarnings,
            List<String> timelineViolations,
            List<String> conflictNotes) {
    }
}
