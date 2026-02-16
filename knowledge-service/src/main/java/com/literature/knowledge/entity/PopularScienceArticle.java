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
        AI("ai"),
        HUMAN("human"),
        HYBRID("hybrid");

        @com.baomidou.mybatisplus.annotation.EnumValue
        @com.fasterxml.jackson.annotation.JsonValue
        private final String value;

        AuthorType(String value) {
            this.value = value;
        }
    }

    public enum Status {
        DRAFT("draft"),
        REVIEWING("reviewing"),
        PUBLISHED("published"),
        ARCHIVED("archived");

        @com.baomidou.mybatisplus.annotation.EnumValue
        @com.fasterxml.jackson.annotation.JsonValue
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
