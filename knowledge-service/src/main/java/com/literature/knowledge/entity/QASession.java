package com.literature.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 问答会话实体
 */
@Data
@TableName(value = "qa_sessions", autoResultMap = true)
public class QASession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private Status status = Status.ACTIVE;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> context;

    private Integer messageCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE("active"),
        CLOSED("closed"),
        ARCHIVED("archived");

        @com.baomidou.mybatisplus.annotation.EnumValue
        @com.fasterxml.jackson.annotation.JsonValue
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
