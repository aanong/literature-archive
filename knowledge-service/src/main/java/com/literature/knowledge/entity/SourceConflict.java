package com.literature.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 史观冲突实体
 * 记录同一历史事件/人物在不同史书中的不同记载
 */
@Data
@TableName(value = "source_conflicts", autoResultMap = true)
public class SourceConflict {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联知识条目ID
     */
    private Long knowledgeItemId;

    /**
     * 冲突主题（如"赤壁之战曹军规模"）
     */
    private String topic;

    /**
     * 各方观点列表
     * JSON: [{source: "史记", bookId: 1, claim: "...", reliability: 1.0}, ...]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Perspective> perspectives;

    /**
     * 学术共识/编者按
     */
    private String resolution;

    /**
     * 解决状态
     */
    private ResolutionType resolutionType = ResolutionType.UNRESOLVED;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ResolutionType {
        UNRESOLVED, // 未解决，各说各话
        CONSENSUS, // 已有学术共识
        DOMINANT_VIEW // 有主流观点但仍有争议
    }

    /**
     * 单个观点/视角
     */
    @Data
    public static class Perspective {
        /**
         * 史料来源名称（如"《史记·项羽本纪》"）
         */
        private String source;

        /**
         * 关联书目ID
         */
        private Long bookId;

        /**
         * 该来源的具体说法
         */
        private String claim;

        /**
         * 史料可信度评级（0.0-1.0）
         */
        private double reliability;

        /**
         * 来源类型（用于匹配权威性排序）
         */
        private String sourceType;
    }
}
