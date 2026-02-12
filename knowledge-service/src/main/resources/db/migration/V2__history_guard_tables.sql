-- ============================================
-- History Guard Tables
-- Version: 2.0
-- Description: 历史科普防护系统表结构（时间线 + 史观冲突）
-- ============================================

-- 历史时间线表
CREATE TABLE IF NOT EXISTS historical_timelines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '时间线ID',
    knowledge_item_id BIGINT COMMENT '关联知识条目ID',
    dynasty VARCHAR(50) COMMENT '朝代',
    era_name VARCHAR(50) COMMENT '年号',
    era_year INT COMMENT '年号年数',
    gregorian_year INT COMMENT '公元年（负数=公元前）',
    month_day VARCHAR(20) COMMENT '月日',
    `precision` ENUM('EXACT', 'APPROXIMATE', 'CENTURY') NOT NULL DEFAULT 'EXACT' COMMENT '时间精度',
    event_type ENUM('BIRTH', 'DEATH', 'EVENT', 'REIGN_START', 'REIGN_END', 'BATTLE', 'POLICY', 'CULTURAL') NOT NULL COMMENT '事件类型',
    description VARCHAR(500) COMMENT '事件简述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_knowledge_item_id (knowledge_item_id),
    INDEX idx_dynasty (dynasty),
    INDEX idx_gregorian_year (gregorian_year),
    INDEX idx_event_type (event_type),
    INDEX idx_era_name (era_name),
    CONSTRAINT fk_timeline_knowledge FOREIGN KEY (knowledge_item_id) REFERENCES knowledge_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史时间线表';

-- 史观冲突记录表
CREATE TABLE IF NOT EXISTS source_conflicts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '冲突ID',
    knowledge_item_id BIGINT COMMENT '关联知识条目ID',
    topic VARCHAR(200) NOT NULL COMMENT '冲突主题',
    perspectives JSON COMMENT '各方观点 [{source, bookId, claim, reliability, sourceType}]',
    resolution TEXT COMMENT '学术共识/编者按',
    resolution_type ENUM('UNRESOLVED', 'CONSENSUS', 'DOMINANT_VIEW') NOT NULL DEFAULT 'UNRESOLVED' COMMENT '解决状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_knowledge_item_id (knowledge_item_id),
    INDEX idx_resolution_type (resolution_type),
    FULLTEXT INDEX ft_topic (topic) WITH PARSER ngram,
    CONSTRAINT fk_conflict_knowledge FOREIGN KEY (knowledge_item_id) REFERENCES knowledge_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='史观冲突记录表';
