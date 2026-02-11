-- ============================================
-- Knowledge Service Database Schema
-- Version: 1.0
-- Description: AI科普知识服务核心表结构
-- ============================================

-- 典籍知识条目表
CREATE TABLE IF NOT EXISTS knowledge_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识条目ID',
    book_id BIGINT COMMENT '关联书目ID',
    chapter_id BIGINT COMMENT '关联章节ID',
    title VARCHAR(500) NOT NULL COMMENT '知识点标题',
    content TEXT NOT NULL COMMENT '知识点内容',
    source_text TEXT COMMENT '原文引用',
    category VARCHAR(50) NOT NULL COMMENT '分类(philosophy/history/literature/culture)',
    tags JSON COMMENT '标签列表',
    embedding_id VARCHAR(100) COMMENT '向量ID(Milvus)',
    status ENUM('draft', 'published', 'archived') NOT NULL DEFAULT 'draft' COMMENT '状态',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_id (book_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_embedding_id (embedding_id),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='典籍知识条目表';

-- 科普文章表
CREATE TABLE IF NOT EXISTS popular_science_articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    title VARCHAR(500) NOT NULL COMMENT '文章标题',
    content LONGTEXT NOT NULL COMMENT '文章内容(Markdown格式)',
    summary TEXT COMMENT '摘要',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    related_knowledge_ids JSON COMMENT '关联知识点ID列表',
    source_books JSON COMMENT '引用书目信息',
    topic VARCHAR(200) COMMENT '主题',
    author_type ENUM('ai', 'human', 'hybrid') NOT NULL DEFAULT 'ai' COMMENT '作者类型',
    status ENUM('draft', 'reviewing', 'published', 'archived') NOT NULL DEFAULT 'draft' COMMENT '状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    created_by BIGINT COMMENT '创建者ID',
    reviewed_by BIGINT COMMENT '审核者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    published_at TIMESTAMP COMMENT '发布时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_topic (topic),
    INDEX idx_author_type (author_type),
    INDEX idx_published_at (published_at),
    FULLTEXT INDEX ft_title_summary (title, summary) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普文章表';

-- 问答会话表
CREATE TABLE IF NOT EXISTS qa_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(500) COMMENT '会话标题',
    status ENUM('active', 'closed', 'archived') NOT NULL DEFAULT 'active' COMMENT '状态',
    context JSON COMMENT '会话上下文(最近N轮对话)',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_message_at TIMESTAMP COMMENT '最后消息时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答会话表';

-- 问答消息表
CREATE TABLE IF NOT EXISTS qa_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role ENUM('user', 'assistant', 'system') NOT NULL COMMENT '角色',
    content TEXT NOT NULL COMMENT '消息内容',
    related_knowledge_ids JSON COMMENT '相关知识点ID列表',
    sources JSON COMMENT '引用来源信息',
    model_name VARCHAR(100) COMMENT '使用的模型名称',
    tokens_used INT COMMENT '使用的token数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_qa_message_session FOREIGN KEY (session_id) REFERENCES qa_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答消息表';

-- 知识推荐表
CREATE TABLE IF NOT EXISTS knowledge_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '推荐ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识点ID',
    score DECIMAL(5,4) NOT NULL COMMENT '推荐分数(0-1)',
    reason VARCHAR(500) COMMENT '推荐理由',
    status ENUM('pending', 'viewed', 'liked', 'dismissed') NOT NULL DEFAULT 'pending' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    viewed_at TIMESTAMP COMMENT '查看时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_status (status),
    INDEX idx_score (score),
    CONSTRAINT fk_recommendation_knowledge FOREIGN KEY (knowledge_id) REFERENCES knowledge_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识推荐表';

-- 文章生成任务表
CREATE TABLE IF NOT EXISTS article_generation_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_id VARCHAR(100) UNIQUE NOT NULL COMMENT '任务唯一标识',
    topic VARCHAR(500) NOT NULL COMMENT '主题',
    related_book_ids JSON COMMENT '关联书目ID列表',
    style VARCHAR(50) COMMENT '风格(popular/academic/narrative)',
    target_audience VARCHAR(50) COMMENT '目标读者(general/student/scholar)',
    length VARCHAR(50) COMMENT '长度(short/medium/long)',
    status ENUM('pending', 'processing', 'completed', 'failed') NOT NULL DEFAULT 'pending' COMMENT '状态',
    progress INT DEFAULT 0 COMMENT '进度(0-100)',
    article_id BIGINT COMMENT '生成的文章ID',
    error_message TEXT COMMENT '错误信息',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    started_at TIMESTAMP COMMENT '开始时间',
    completed_at TIMESTAMP COMMENT '完成时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    CONSTRAINT fk_task_article FOREIGN KEY (article_id) REFERENCES popular_science_articles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章生成任务表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type ENUM('knowledge', 'article', 'qa_message', 'recommendation') NOT NULL COMMENT '目标类型',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    feedback_type ENUM('like', 'dislike', 'report', 'comment') NOT NULL COMMENT '反馈类型',
    content TEXT COMMENT '反馈内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_feedback_type (feedback_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';
