package com.whut.ai.controller;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import com.whut.ai.service.KnowledgeBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/build")
    public ResponseEntity<Map<String, Object>> build(@RequestBody KnowledgeBaseBuildRequest request) {
        log.info("收到知识库构建请求 | 路径: {}", request.getDocumentPath());
        try {
            KnowledgeBaseService.BuildResult result = knowledgeBaseService.buildKnowledgeBase(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalChunks", result.totalChunks(),
                    "totalDocuments", result.totalDocuments(),
                    "sourcePath", result.sourcePath(),
                    "message", "知识库构建成功！"
            ));
        } catch (Exception e) {
            log.error("知识库构建失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "4") int topK) {
        log.info("收到语义检索请求 | 查询: {} | TopK: {}", query, topK);
        try {
            List<DocumentChunk> results = knowledgeBaseService.searchSimilar(query, topK);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "total", results.size(),
                    "results", results
            ));
        } catch (Exception e) {
            log.error("检索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
