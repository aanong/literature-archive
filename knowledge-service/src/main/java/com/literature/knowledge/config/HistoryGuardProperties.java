package com.literature.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 历史科普防护配置
 * 绑定 application.yml 中 history-guard.* 配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "history-guard")
public class HistoryGuardProperties {

    private boolean enabled = true;

    private AntiHallucination antiHallucination = new AntiHallucination();
    private Timeline timeline = new Timeline();
    private Conflict conflict = new Conflict();

    // ====== 防幻觉配置 ======
    @Data
    public static class AntiHallucination {
        /**
         * 严格等级: LENIENT(宽松) / STANDARD(标准) / STRICT(严格)
         */
        private Level level = Level.STANDARD;

        /**
         * 回答是否必须引用原文出处
         */
        private boolean requireSourceCitation = true;

        /**
         * 允许的最大无出处论断数（STRICT模式自动设为0）
         */
        private int maxUnsourcedClaims = 2;

        /**
         * 向量检索匹配阈值；低于此值标记为"待考证"
         */
        private double confidenceThreshold = 0.75;

        /**
         * 是否开启二次事实核验（用LLM对答案做交叉检查）
         */
        private boolean factCheckEnabled = true;

        /**
         * 禁止出现的绝对化表述
         */
        private List<String> forbiddenPhrases = Arrays.asList(
                "众所周知", "毫无疑问", "历史证明", "不可否认", "铁板钉钉");

        /**
         * 不确定标注配置
         */
        private UncertaintyMarkers uncertaintyMarkers = new UncertaintyMarkers();

        public enum Level {
            LENIENT, STANDARD, STRICT
        }

        @Data
        public static class UncertaintyMarkers {
            private String prefix = "⚠️ [待考证]";
            private String suffix = "（此说法尚需查证更多史料）";
        }
    }

    // ====== 时间线配置 ======
    @Data
    public static class Timeline {
        private boolean enabled = true;

        /**
         * 是否自动从文本中提取时间信息
         */
        private boolean autoExtract = true;

        /**
         * 纪年体系: GREGORIAN / CHINESE_ERA / MIXED
         */
        private CalendarSystem calendarSystem = CalendarSystem.MIXED;

        /**
         * 校验时间线逻辑（如人物生卒年与事件年份）
         */
        private boolean validateChronology = true;

        /**
         * 日期输出格式
         */
        private String dateFormat = "公元{year}年（{dynasty}{era}{era_year}年）";

        public enum CalendarSystem {
            GREGORIAN, CHINESE_ERA, MIXED
        }
    }

    // ====== 史观冲突配置 ======
    @Data
    public static class Conflict {
        private boolean enabled = true;

        /**
         * 冲突呈现策略:
         * SINGLE_AUTHORITY — 只呈现最权威来源
         * MULTI_VIEW — 呈现多方观点并标注可信度
         * SCHOLARLY_CONSENSUS — 优先呈现学术共识
         */
        private ResolutionStrategy resolutionStrategy = ResolutionStrategy.MULTI_VIEW;

        /**
         * 是否展示史料可信度评级
         */
        private boolean showSourceReliability = true;

        /**
         * 最多展示几种观点
         */
        private int maxPerspectives = 3;

        /**
         * 史料权威性排序
         */
        private List<AuthorityRanking> authorityRanking = Arrays.asList(
                new AuthorityRanking("正史（二十四史）", 1.0),
                new AuthorityRanking("编年体（资治通鉴）", 0.95),
                new AuthorityRanking("现代学术研究", 0.9),
                new AuthorityRanking("纪事本末体", 0.85),
                new AuthorityRanking("笔记/野史", 0.6));

        public enum ResolutionStrategy {
            SINGLE_AUTHORITY, MULTI_VIEW, SCHOLARLY_CONSENSUS
        }

        @Data
        public static class AuthorityRanking {
            private String name;
            private double weight;

            public AuthorityRanking() {
            }

            public AuthorityRanking(String name, double weight) {
                this.name = name;
                this.weight = weight;
            }
        }
    }
}
