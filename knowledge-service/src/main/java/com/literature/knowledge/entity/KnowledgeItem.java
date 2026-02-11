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
 * 典籍知识条目实体
 */
@Data
@TableName(value = "knowledge_items", autoResultMap = true)
public class KnowledgeItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long bookId;
    
    private Long chapterId;
    
    private String title;
    
    private String content;
    
    private String sourceText;
    
    private String category;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    
    private String embeddingId;
    
    private Status status = Status.DRAFT;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum Status {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
