package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.entity.PopularScienceArticle;
import com.literature.knowledge.mapper.PopularScienceArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 科普文章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularScienceService {

    private final PopularScienceArticleMapper articleMapper;
    private final LangChainService langChainService;
    private final HistoryGuardService historyGuardService;

    /**
     * AI生成科普文章
     */
    @Transactional
    public PopularScienceArticle generateArticle(String topic, String requirement) {
        // 1. 构建提示词
        String prompt = String.format("""
                请撰写一篇关于"%s"的科普文章。
                要求: %s
                文章结构应包含:引言、正文(分小标题)、结语。
                请使用Markdown格式输出。
                """, topic, requirement);

        // 2. 用历史防护服务增强 prompt
        String guardedPrompt = historyGuardService.buildGuardedSystemPrompt(prompt);

        // 3. 调用AI生成
        String content = langChainService.chat(guardedPrompt);

        // 4. 防幻觉后处理
        HistoryGuardService.GuardedResponse guarded = historyGuardService.guardAnswer(content, topic, List.of());
        content = guarded.answer();

        // 5. 保存文章草稿
        PopularScienceArticle article = new PopularScienceArticle();
        article.setTitle(topic);
        article.setTopic(topic);
        article.setContent(content);
        article.setSummary(content.length() > 100 ? content.substring(0, 100) + "..." : content);
        article.setAuthorType(PopularScienceArticle.AuthorType.AI);
        article.setStatus(PopularScienceArticle.Status.DRAFT);
        article.setCreatedAt(LocalDateTime.now());

        articleMapper.insert(article);
        return article;
    }

    /**
     * 获取文章详情
     */
    public Optional<PopularScienceArticle> getArticle(Long id) {
        return Optional.ofNullable(articleMapper.selectById(id));
    }

    /**
     * 获取所有文章
     */
    public List<PopularScienceArticle> getAllArticles() {
        return articleMapper.selectList(null);
    }

    /**
     * 发布文章
     */
    @Transactional
    public PopularScienceArticle publishArticle(Long id) {
        PopularScienceArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("Article not found: " + id);
        }

        article.setStatus(PopularScienceArticle.Status.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        articleMapper.updateById(article);
        return article;
    }
}
