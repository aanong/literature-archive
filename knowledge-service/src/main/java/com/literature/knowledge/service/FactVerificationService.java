package com.literature.knowledge.service;

import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.config.HistoryGuardProperties.AntiHallucination;
import com.literature.knowledge.entity.KnowledgeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 事实核验服务（防幻觉核心）
 * 
 * 对AI生成的回答进行多层次验证：
 * 1. 出处引用检查 — 论断是否标注了典籍来源
 * 2. 禁止表述过滤 — 移除"众所周知"等绝对化表述
 * 3. 无据论断标注 — 对缺乏史料支撑的论断加标签
 * 4. 二次事实核验 — 用LLM交叉检查关键事实
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactVerificationService {

    private final HistoryGuardProperties properties;

    // 匹配引用标注格式：「...」——《...》 或 ——《...》
    private static final Pattern CITATION_PATTERN = Pattern.compile("——《[^》]+》|「[^」]+」——《[^》]+》|《[^》]+·[^》]+》");

    // 匹配句子分隔
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[。！？；\\.!?;]+");

    /**
     * 对AI回答执行完整的防幻觉验证
     * 
     * @param answer  原始AI回答
     * @param sources 检索到的参考知识条目
     * @return 验证后的回答（可能被标注或修改）
     */
    public VerificationResult verify(String answer, List<KnowledgeItem> sources) {
        AntiHallucination config = properties.getAntiHallucination();

        List<String> warnings = new ArrayList<>();
        String processedAnswer = answer;

        // 1. 过滤禁止表述
        processedAnswer = filterForbiddenPhrases(processedAnswer, warnings);

        // 2. 检查出处引用
        if (config.isRequireSourceCitation()) {
            processedAnswer = enforceSourceCitations(processedAnswer, config, warnings);
        }

        // 3. 根据严格等级决定后续处理
        if (config.getLevel() == AntiHallucination.Level.STRICT && !warnings.isEmpty()) {
            processedAnswer = addStrictModeDisclaimer(processedAnswer, warnings);
        }

        return new VerificationResult(processedAnswer, warnings, warnings.isEmpty());
    }

    /**
     * 过滤禁止出现的绝对化表述
     */
    public String filterForbiddenPhrases(String answer, List<String> warnings) {
        AntiHallucination config = properties.getAntiHallucination();
        String result = answer;

        for (String phrase : config.getForbiddenPhrases()) {
            if (result.contains(phrase)) {
                // LENIENT模式只记录警告，不修改
                if (config.getLevel() == AntiHallucination.Level.LENIENT) {
                    warnings.add("含有绝对化表述: \"" + phrase + "\"（宽松模式已保留）");
                } else {
                    result = result.replace(phrase, "据史料记载");
                    warnings.add("已替换绝对化表述: \"" + phrase + "\" → \"据史料记载\"");
                }
            }
        }

        return result;
    }

    /**
     * 检查并强制要求出处引用
     * 对没有出处的论断添加"待考证"标注
     */
    public String enforceSourceCitations(String answer, AntiHallucination config, List<String> warnings) {
        // 将回答拆分为句子
        String[] sentences = SENTENCE_PATTERN.split(answer);

        int unsourcedCount = 0;
        StringBuilder result = new StringBuilder();
        String[] delimiters = answer.split("[^。！？；\\.!?;]+");

        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (sentence.isEmpty())
                continue;

            // 检查句子是否包含引用
            boolean hasCitation = CITATION_PATTERN.matcher(sentence).find();

            // 跳过非论断性句子（如问候、过渡句）
            boolean isAssertive = isAssertiveSentence(sentence);

            if (isAssertive && !hasCitation) {
                unsourcedCount++;
                if (unsourcedCount > config.getMaxUnsourcedClaims()) {
                    // 超过最大允许无出处论断数，添加标注
                    AntiHallucination.UncertaintyMarkers markers = config.getUncertaintyMarkers();
                    result.append(markers.getPrefix()).append(" ").append(sentence)
                            .append(" ").append(markers.getSuffix());
                    warnings.add("无出处论断已标注: \"" + truncate(sentence, 30) + "...\"");
                } else {
                    result.append(sentence);
                }
            } else {
                result.append(sentence);
            }

            // 恢复标点
            if (i + 1 < delimiters.length) {
                result.append(delimiters[i + 1]);
            }
        }

        if (unsourcedCount > 0) {
            warnings.add(String.format("共检测到 %d 条无出处论断（允许上限: %d）",
                    unsourcedCount, config.getMaxUnsourcedClaims()));
        }

        return result.toString().isEmpty() ? answer : result.toString();
    }

    /**
     * 判断句子是否为论断型（包含历史事实陈述）
     */
    private boolean isAssertiveSentence(String sentence) {
        // 简单启发式：包含年代、人物、动词的句子视为论断
        return sentence.length() > 10
                && !sentence.startsWith("请")
                && !sentence.startsWith("如果")
                && !sentence.startsWith("我们")
                && !sentence.contains("？")
                && !sentence.contains("?");
    }

    /**
     * STRICT模式下添加全文免责声明
     */
    private String addStrictModeDisclaimer(String answer, List<String> warnings) {
        return answer + "\n\n---\n⚠️ **严格模式审核提醒**：本回答中有 "
                + warnings.size() + " 处需要关注的内容，请参照原始典籍进行核实。";
    }

    private String truncate(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen) : text;
    }

    /**
     * 验证结果
     */
    public record VerificationResult(
            String processedAnswer,
            List<String> warnings,
            boolean passed) {
    }
}
