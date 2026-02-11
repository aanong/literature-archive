package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.config.HistoryGuardProperties.Conflict;
import com.literature.knowledge.entity.SourceConflict;
import com.literature.knowledge.mapper.SourceConflictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 史观冲突处理服务
 * 
 * 支持三种冲突呈现策略：
 * - SINGLE_AUTHORITY — 只呈现最权威来源的说法
 * - MULTI_VIEW — 并列呈现多方观点，标注可信度
 * - SCHOLARLY_CONSENSUS — 优先呈现学术共识，附注其他看法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictResolutionService {

    private final SourceConflictMapper conflictMapper;
    private final HistoryGuardProperties properties;

    /**
     * 创建冲突记录
     */
    @Transactional
    public SourceConflict create(SourceConflict conflict) {
        conflict.setCreatedAt(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflictMapper.insert(conflict);
        return conflict;
    }

    /**
     * 查找知识条目的所有冲突
     */
    public List<SourceConflict> findConflicts(Long knowledgeItemId) {
        return conflictMapper.selectList(
                new LambdaQueryWrapper<SourceConflict>()
                        .eq(SourceConflict::getKnowledgeItemId, knowledgeItemId)
                        .orderByDesc(SourceConflict::getCreatedAt));
    }

    /**
     * 添加编者按/学术共识
     */
    @Transactional
    public SourceConflict resolve(Long conflictId, String resolution, SourceConflict.ResolutionType type) {
        SourceConflict conflict = conflictMapper.selectById(conflictId);
        if (conflict == null) {
            throw new RuntimeException("冲突记录不存在: " + conflictId);
        }
        conflict.setResolution(resolution);
        conflict.setResolutionType(type);
        conflict.setUpdatedAt(LocalDateTime.now());
        conflictMapper.updateById(conflict);
        return conflict;
    }

    /**
     * 根据配置策略格式化冲突的展示文本
     * 这段文本将被注入到AI回答中
     */
    public String formatConflictResponse(SourceConflict conflict) {
        Conflict config = properties.getConflict();

        return switch (config.getResolutionStrategy()) {
            case SINGLE_AUTHORITY -> formatSingleAuthority(conflict);
            case MULTI_VIEW -> formatMultiView(conflict, config);
            case SCHOLARLY_CONSENSUS -> formatScholarlyConsensus(conflict);
        };
    }

    /**
     * 格式化所有与知识条目关联的冲突
     * 如果无冲突返回空字符串
     */
    public String formatAllConflicts(Long knowledgeItemId) {
        if (!properties.getConflict().isEnabled()) {
            return "";
        }

        List<SourceConflict> conflicts = findConflicts(knowledgeItemId);
        if (conflicts.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("\n\n📚 **史料记载差异**\n\n");
        for (SourceConflict conflict : conflicts) {
            sb.append("**").append(conflict.getTopic()).append("**\n");
            sb.append(formatConflictResponse(conflict));
            sb.append("\n\n");
        }
        return sb.toString();
    }

    // ====== 私有方法：三种策略的格式化实现 ======

    /**
     * SINGLE_AUTHORITY: 只展示最权威来源的说法
     */
    private String formatSingleAuthority(SourceConflict conflict) {
        if (conflict.getPerspectives() == null || conflict.getPerspectives().isEmpty()) {
            return "暂无相关记载。";
        }

        // 按可信度降序取第一个
        SourceConflict.Perspective best = conflict.getPerspectives().stream()
                .max(Comparator.comparingDouble(SourceConflict.Perspective::getReliability))
                .orElse(conflict.getPerspectives().get(0));

        StringBuilder sb = new StringBuilder();
        sb.append("据").append(best.getSource()).append("记载：").append(best.getClaim());

        if (conflict.getResolution() != null) {
            sb.append("\n\n> 💡 ").append(conflict.getResolution());
        }
        return sb.toString();
    }

    /**
     * MULTI_VIEW: 并列展示多方观点
     */
    private String formatMultiView(SourceConflict conflict, Conflict config) {
        if (conflict.getPerspectives() == null || conflict.getPerspectives().isEmpty()) {
            return "暂无相关记载。";
        }

        // 按可信度降序排列，取前 maxPerspectives 个
        List<SourceConflict.Perspective> views = conflict.getPerspectives().stream()
                .sorted(Comparator.comparingDouble(SourceConflict.Perspective::getReliability).reversed())
                .limit(config.getMaxPerspectives())
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < views.size(); i++) {
            SourceConflict.Perspective p = views.get(i);
            sb.append(i + 1).append(". ").append(p.getSource()).append("：").append(p.getClaim());
            if (config.isShowSourceReliability()) {
                sb.append(" (可信度: ").append(String.format("%.0f%%", p.getReliability() * 100)).append(")");
            }
            sb.append("\n");
        }

        if (conflict.getResolution() != null) {
            sb.append("\n> 💡 **编者按**: ").append(conflict.getResolution());
        }
        return sb.toString();
    }

    /**
     * SCHOLARLY_CONSENSUS: 优先呈现学术共识
     */
    private String formatScholarlyConsensus(SourceConflict conflict) {
        StringBuilder sb = new StringBuilder();

        if (conflict.getResolutionType() == SourceConflict.ResolutionType.CONSENSUS
                && conflict.getResolution() != null) {
            sb.append("📖 **学术共识**: ").append(conflict.getResolution()).append("\n\n");
            sb.append("其他观点参考：\n");
        } else if (conflict.getResolutionType() == SourceConflict.ResolutionType.DOMINANT_VIEW
                && conflict.getResolution() != null) {
            sb.append("📖 **主流观点**: ").append(conflict.getResolution()).append("\n\n");
            sb.append("其他观点参考：\n");
        } else {
            sb.append("⚠️ 此问题尚无学术共识，各方观点如下：\n");
        }

        if (conflict.getPerspectives() != null) {
            for (SourceConflict.Perspective p : conflict.getPerspectives()) {
                sb.append("- ").append(p.getSource()).append("：").append(p.getClaim()).append("\n");
            }
        }

        return sb.toString();
    }
}
