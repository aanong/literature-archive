-- ============================================
-- Chat Service Database Schema
-- Version: 1.0
-- Description: 聊天服务核心表结构
-- ============================================

-- 会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    title VARCHAR(255) COMMENT '会话标题（群聊名称）',
    type ENUM('private', 'group') NOT NULL DEFAULT 'private' COMMENT '会话类型：私聊/群聊',
    status ENUM('active', 'closed', 'archived') NOT NULL DEFAULT 'active' COMMENT '会话状态',
    created_by BIGINT NOT NULL COMMENT '创建者用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_created_by (created_by),
    INDEX idx_status (status),
    INDEX idx_type_status (type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 会话成员表
CREATE TABLE IF NOT EXISTS chat_session_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('owner', 'admin', 'member') NOT NULL DEFAULT 'member' COMMENT '成员角色',
    nickname VARCHAR(100) COMMENT '群内昵称',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_read_at TIMESTAMP COMMENT '最后阅读时间',
    UNIQUE KEY uk_session_user (session_id, user_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_member_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员表';

-- 消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
    content TEXT NOT NULL COMMENT '消息内容',
    content_type ENUM('text', 'image', 'file', 'audio', 'video', 'system') NOT NULL DEFAULT 'text' COMMENT '消息类型',
    status ENUM('sending', 'sent', 'delivered', 'read', 'deleted', 'recalled') NOT NULL DEFAULT 'sent' COMMENT '消息状态',
    reply_to_id BIGINT COMMENT '回复的消息ID',
    extra JSON COMMENT '扩展字段（图片URL、文件信息等）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX idx_session_created (session_id, created_at DESC),
    INDEX idx_sender (sender_id),
    INDEX idx_status (status),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 禁言记录表
CREATE TABLE IF NOT EXISTS chat_mutes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '被禁言用户ID',
    muted_until TIMESTAMP NOT NULL COMMENT '禁言结束时间',
    reason VARCHAR(500) COMMENT '禁言原因',
    created_by BIGINT NOT NULL COMMENT '操作者用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_user (session_id, user_id),
    INDEX idx_muted_until (muted_until),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_mute_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='禁言记录表';

-- 封禁记录表
CREATE TABLE IF NOT EXISTS chat_bans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '被封禁用户ID',
    status ENUM('active', 'lifted') NOT NULL DEFAULT 'active' COMMENT '封禁状态',
    reason VARCHAR(500) COMMENT '封禁原因',
    created_by BIGINT NOT NULL COMMENT '操作者用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    lifted_at TIMESTAMP COMMENT '解封时间',
    lifted_by BIGINT COMMENT '解封操作者',
    INDEX idx_session_user (session_id, user_id),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_ban_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='封禁记录表';

-- 消息已读记录表（用于群聊已读统计）
CREATE TABLE IF NOT EXISTS chat_message_reads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '阅读者用户ID',
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_read_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息已读记录表';
