package com.whut.ai.controller;

import com.whut.ai.dto.DocumentUploadRequest;
import com.whut.ai.dto.RagQueryRequest;
import com.whut.ai.rag.DocumentService;
import com.whut.ai.rag.RagService;
import com.whut.ai.vo.RagQueryResponse;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 知识库管理接口。
 * 提供文档上传、向量化索引和语义检索能力。
 */
@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;
    private final DocumentService documentService;

    public RagController(RagService ragService, DocumentService documentService) {
        this.ragService = ragService;
        this.documentService = documentService;
    }

    /**
     * 知识库检索
     */
    @PostMapping("/search")
    public Result<RagQueryResponse> search(@RequestBody RagQueryRequest request) {
        return Result.success(ragService.search(request));
    }

    /**
     * 上传文档并向量化索引
     */
    @PostMapping("/documents")
    public Result<String> uploadDocument(@RequestBody DocumentUploadRequest request) {
        documentService.uploadAndIndex(request);
        return Result.success("文档上传并索引成功");
    }
}
