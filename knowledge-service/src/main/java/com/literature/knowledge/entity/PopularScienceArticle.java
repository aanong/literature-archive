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
 * 科普文章实体
 */
@Data
@TableName(value = "popular_science_articles", autoResultMap = true)
public class PopularScienceArticle {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private String summary;
    
    private String coverImage;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> relatedKnowledgeIds;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> sourceBooks;
    
    private String topic;
    
    private AuthorType authorType = AuthorType.AI;
    
    private Status status = Status.DRAFT;
    
    private Integer viewCount = 0;
    
    private Integer likeCount = 0;
    
    private Long createdBy;
    
    private Long reviewedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime publishedAt;
    
    private LocalDateTime updatedAt;
    
    public enum AuthorType {
        AI, HUMAN, HYBRID
    }
    
    public enum Status {
        DRAFT, REVIEWING, PUBLISHED, ARCHIVED
    }
}
