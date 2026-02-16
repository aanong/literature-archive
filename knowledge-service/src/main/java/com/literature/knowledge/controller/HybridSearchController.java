package com.literature.knowledge.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.knowledge.service.HybridSearchService;
import com.literature.knowledge.service.HybridSearchService.HybridSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 混合搜索控制器
 * <p>
 * 提供结合向量语义搜索与关键词搜索的混合搜索 API。
 * </p>
 */
@RestController
@RequestMapping("/api/knowledge/hybrid-search")
@RequiredArgsConstructor
public class HybridSearchController {

    private final HybridSearchService hybridSearchService;

    /**
     * 混合搜索
     *
     * @param query          搜索关键词/自然语言查询
     * @param maxResults     最大返回条数，默认 10
     * @param semanticWeight 语义搜索权重 (0.0~1.0)，默认 0.7
     * @return 搜索结果列表
     */
    @GetMapping
    public ApiResponse<List<HybridSearchResult>> hybridSearch(
            @RequestParam("query") String query,
            @RequestParam(name = "maxResults", defaultValue = "10") int maxResults,
            @RequestParam(name = "semanticWeight", defaultValue = "0.7") double semanticWeight) {
        List<HybridSearchResult> results = hybridSearchService.search(query, maxResults, semanticWeight);
        return ApiResponse.success(results, null);
    }
}
