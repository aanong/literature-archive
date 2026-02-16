ALTER TABLE chat_session_members
    ADD COLUMN member_type VARCHAR(16) NOT NULL DEFAULT 'ADMIN' COMMENT '成员类型: ADMIN/C_USER' AFTER user_id,
    DROP INDEX uk_session_user,
    ADD UNIQUE KEY uk_session_user_type (session_id, user_id, member_type),
    ADD INDEX idx_user_type (user_id, member_type);

ALTER TABLE chat_messages
    ADD COLUMN sender_type VARCHAR(16) NOT NULL DEFAULT 'ADMIN' COMMENT '发送者类型: ADMIN/C_USER' AFTER sender_id,
    DROP INDEX idx_sender,
    ADD INDEX idx_sender_type (sender_id, sender_type);
