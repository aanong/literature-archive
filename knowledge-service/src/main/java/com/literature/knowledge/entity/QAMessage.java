package com.literature.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问答消息实体
 */
@Data
@TableName(value = "qa_messages", autoResultMap = true)
public class QAMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sessionId;
    
    private Role role;
    
    private String content;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> relatedKnowledgeIds;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> sources;
    
    private String modelName;
    
    private Integer tokensUsed;
    
    private LocalDateTime createdAt;
    
    public enum Role {
        USER, ASSISTANT, SYSTEM
    }
}
