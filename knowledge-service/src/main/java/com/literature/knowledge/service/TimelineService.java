package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.entity.HistoricalTimeline;
import com.literature.knowledge.mapper.HistoricalTimelineMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 时间线管理服务
 * 
 * 管理历史事件的时间信息，支持：
 * - 中国年号纪年 ↔ 公元纪年转换
 * - 按朝代/年份范围查询
 * - 时间线逻辑一致性校验
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final HistoricalTimelineMapper timelineMapper;
    private final HistoryGuardProperties properties;

    /**
     * 创建时间线条目
     */
    @Transactional
    public HistoricalTimeline create(HistoricalTimeline timeline) {
        timeline.setCreatedAt(LocalDateTime.now());
        timeline.setUpdatedAt(LocalDateTime.now());
        timelineMapper.insert(timeline);
        return timeline;
    }

    /**
     * 批量创建时间线条目
     */
    @Transactional
    public void batchCreate(List<HistoricalTimeline> timelines) {
        for (HistoricalTimeline t : timelines) {
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            timelineMapper.insert(t);
        }
        log.info("批量创建时间线条目: {} 条", timelines.size());
    }

    /**
     * 获取知识条目的时间线
     */
    public List<HistoricalTimeline> getTimeline(Long knowledgeItemId) {
        return timelineMapper.selectList(
                new LambdaQueryWrapper<HistoricalTimeline>()
                        .eq(HistoricalTimeline::getKnowledgeItemId, knowledgeItemId)
                        .orderByAsc(HistoricalTimeline::getGregorianYear));
    }

    /**
     * 按朝代查询时间线
     */
    public List<HistoricalTimeline> getTimelineByDynasty(String dynasty) {
        return timelineMapper.selectList(
                new LambdaQueryWrapper<HistoricalTimeline>()
                        .eq(HistoricalTimeline::getDynasty, dynasty)
                        .orderByAsc(HistoricalTimeline::getGregorianYear));
    }

    /**
     * 按公元年范围查询（支持公元前，使用负数）
     */
    public List<HistoricalTimeline> getTimelineRange(int startYear, int endYear) {
        return timelineMapper.selectList(
                new LambdaQueryWrapper<HistoricalTimeline>()
                        .ge(HistoricalTimeline::getGregorianYear, startYear)
                        .le(HistoricalTimeline::getGregorianYear, endYear)
                        .orderByAsc(HistoricalTimeline::getGregorianYear));
    }

    /**
     * 按事件类型查询
     */
    public List<HistoricalTimeline> getByEventType(HistoricalTimeline.EventType eventType) {
        return timelineMapper.selectList(
                new LambdaQueryWrapper<HistoricalTimeline>()
                        .eq(HistoricalTimeline::getEventType, eventType)
                        .orderByAsc(HistoricalTimeline::getGregorianYear));
    }

    /**
     * 格式化时间显示
     * 根据配置的 dateFormat 模板输出
     */
    public String formatDate(HistoricalTimeline timeline) {
        HistoryGuardProperties.Timeline config = properties.getTimeline();

        return switch (config.getCalendarSystem()) {
            case GREGORIAN -> formatGregorian(timeline);
            case CHINESE_ERA -> formatChineseEra(timeline);
            case MIXED -> formatMixed(timeline, config.getDateFormat());
        };
    }

    /**
     * 校验时间线逻辑一致性
     * 如：人物逝世年份不能早于出生年份
     */
    public List<String> validateChronology(Long knowledgeItemId) {
        if (!properties.getTimeline().isValidateChronology()) {
            return List.of();
        }

        List<HistoricalTimeline> events = getTimeline(knowledgeItemId);
        List<String> violations = new java.util.ArrayList<>();

        // 查找 BIRTH 和 DEATH 事件
        Optional<HistoricalTimeline> birth = events.stream()
                .filter(e -> e.getEventType() == HistoricalTimeline.EventType.BIRTH)
                .findFirst();
        Optional<HistoricalTimeline> death = events.stream()
                .filter(e -> e.getEventType() == HistoricalTimeline.EventType.DEATH)
                .findFirst();

        if (birth.isPresent() && death.isPresent()) {
            if (death.get().getGregorianYear() < birth.get().getGregorianYear()) {
                violations.add(String.format("时间逻辑错误：逝世年（%d）早于出生年（%d）",
                        death.get().getGregorianYear(), birth.get().getGregorianYear()));
            }
            int age = death.get().getGregorianYear() - birth.get().getGregorianYear();
            if (age > 150) {
                violations.add(String.format("疑似错误：寿命异常 %d 年（%d - %d）",
                        age, birth.get().getGregorianYear(), death.get().getGregorianYear()));
            }
        }

        // 校验所有事件是否在生卒年范围内
        if (birth.isPresent() && death.isPresent()) {
            int birthYear = birth.get().getGregorianYear();
            int deathYear = death.get().getGregorianYear();
            for (HistoricalTimeline event : events) {
                if (event.getEventType() != HistoricalTimeline.EventType.BIRTH
                        && event.getEventType() != HistoricalTimeline.EventType.DEATH
                        && event.getGregorianYear() != null) {
                    if (event.getGregorianYear() < birthYear || event.getGregorianYear() > deathYear) {
                        violations.add(String.format("时间逻辑警告：事件\"%s\"（%d年）不在生卒年范围 %d-%d 内",
                                event.getDescription(), event.getGregorianYear(), birthYear, deathYear));
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            log.warn("时间线校验发现 {} 个问题 (知识条目ID={}): {}",
                    violations.size(), knowledgeItemId, violations);
        }

        return violations;
    }

    private String formatGregorian(HistoricalTimeline t) {
        if (t.getGregorianYear() == null)
            return "时间不详";
        String yearStr = t.getGregorianYear() < 0
                ? "公元前" + Math.abs(t.getGregorianYear()) + "年"
                : "公元" + t.getGregorianYear() + "年";
        return t.getMonthDay() != null ? yearStr + t.getMonthDay() : yearStr;
    }

    private String formatChineseEra(HistoricalTimeline t) {
        if (t.getDynasty() == null)
            return formatGregorian(t);
        StringBuilder sb = new StringBuilder();
        sb.append(t.getDynasty());
        if (t.getEraName() != null) {
            sb.append("·").append(t.getEraName());
            if (t.getEraYear() != null) {
                sb.append(t.getEraYear()).append("年");
            }
        }
        if (t.getMonthDay() != null) {
            sb.append(t.getMonthDay());
        }
        return sb.toString();
    }

    private String formatMixed(HistoricalTimeline t, String template) {
        if (t.getGregorianYear() == null)
            return "时间不详";

        String result = template;
        String yearStr = t.getGregorianYear() < 0
                ? "前" + Math.abs(t.getGregorianYear())
                : String.valueOf(t.getGregorianYear());
        result = result.replace("{year}", yearStr);
        result = result.replace("{dynasty}", t.getDynasty() != null ? t.getDynasty() : "");
        result = result.replace("{era}", t.getEraName() != null ? t.getEraName() : "");
        result = result.replace("{era_year}", t.getEraYear() != null ? String.valueOf(t.getEraYear()) : "");

        return result;
    }
}
