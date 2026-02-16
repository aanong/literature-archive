package com.literature.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.literature.knowledge.entity.KnowledgeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeItemMapper extends BaseMapper<KnowledgeItem> {

    @Select("SELECT DISTINCT tag FROM (" +
            "SELECT JSON_UNQUOTE(JSON_EXTRACT(tags, CONCAT('$[', idx, ']'))) AS tag " +
            "FROM knowledge_items, " +
            "(SELECT 0 AS idx UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
            "UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) AS indices " +
            "WHERE JSON_EXTRACT(tags, CONCAT('$[', idx, ']')) IS NOT NULL) AS all_tags " +
            "WHERE tag IS NOT NULL ORDER BY tag")
    List<String> findAllDistinctTags();
}
