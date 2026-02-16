-- C 端用户表（与后台 users 表分离）
CREATE TABLE IF NOT EXISTS c_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'C端用户ID',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_c_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='C端用户表';
