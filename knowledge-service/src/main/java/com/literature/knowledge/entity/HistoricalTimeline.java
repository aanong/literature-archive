package com.literature.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史时间线实体
 * 用于精确管理历史事件的时间标注（支持年号/公元双体系）
 */
@Data
@TableName("historical_timelines")
public class HistoricalTimeline {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联知识条目ID
     */
    private Long knowledgeItemId;

    /**
     * 朝代（秦/汉/唐…）
     */
    private String dynasty;

    /**
     * 年号（建安/贞观/开元…）
     */
    private String eraName;

    /**
     * 年号年数
     */
    private Integer eraYear;

    /**
     * 公元年（负数表示公元前，如 -221 = 公元前221年）
     */
    private Integer gregorianYear;

    /**
     * 月日（可选，如"六月初四"）
     */
    private String monthDay;

    /**
     * 时间精度
     */
    private Precision precision = Precision.EXACT;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 事件简述
     */
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Precision {
        EXACT, // 精确到年/月/日
        APPROXIMATE, // 大约（"约公元200年"）
        CENTURY // 世纪级别（"公元前3世纪"）
    }

    public enum EventType {
        BIRTH, // 出生
        DEATH, // 逝世
        EVENT, // 历史事件
        REIGN_START, // 登基/即位
        REIGN_END, // 退位/亡国
        BATTLE, // 战役
        POLICY, // 政策/制度变革
        CULTURAL // 文化/学术事件
    }
}
