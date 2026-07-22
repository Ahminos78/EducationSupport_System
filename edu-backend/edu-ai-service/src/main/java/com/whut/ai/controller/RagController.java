package com.whut.ai.controller;

import com.whut.ai.dto.RagQueryRequest;
import com.whut.ai.rag.RagService;
import com.whut.ai.vo.RagQueryResponse;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 检索接口。
 * 提供基于向量库的语义检索能力，支持按课程过滤。
 */
@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * RAG 语义检索（支持 courseId 过滤）
     */
    @PostMapping("/search")
    public Result<RagQueryResponse> search(@RequestBody RagQueryRequest request) {
        return Result.success(ragService.search(request));
    }
}
