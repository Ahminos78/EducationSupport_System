package com.whut.ai.controller;

import com.whut.ai.entity.KbDocument;
import com.whut.ai.entity.KbKnowledgeBase;
import com.whut.ai.service.KnowledgeBaseManageService;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/knowledge-base")
public class KnowledgeBaseManageController {

    private final KnowledgeBaseManageService manageService;

    public KnowledgeBaseManageController(KnowledgeBaseManageService manageService) {
        this.manageService = manageService;
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.getOrDefault("description", "");
        if (name == null || name.isBlank()) {
            return Result.fail(400, "知识库名称不能为空");
        }
        KbKnowledgeBase kb = manageService.createKnowledgeBase(name, description, null);
        return Result.success(Map.of(
                "id", kb.getId(),
                "name", kb.getName(),
                "collectionName", kb.getCollectionName()
        ));
    }

    @GetMapping
    public Result<List<KbKnowledgeBase>> list() {
        return Result.success(manageService.listKnowledgeBases());
    }

    @GetMapping("/{kbId}")
    public Result<KbKnowledgeBase> get(@PathVariable Long kbId) {
        return Result.success(manageService.getKnowledgeBase(kbId));
    }

    @PutMapping("/{kbId}")
    public Result<Void> update(@PathVariable Long kbId, @RequestBody Map<String, String> body) {
        manageService.updateKnowledgeBase(kbId, body.get("name"), body.get("description"));
        return Result.success();
    }

    @DeleteMapping("/{kbId}")
    public Result<Void> delete(@PathVariable Long kbId) {
        manageService.deleteKnowledgeBase(kbId);
        return Result.success();
    }

    @GetMapping("/{kbId}/documents")
    public Result<List<KbDocument>> listDocuments(@PathVariable Long kbId) {
        return Result.success(manageService.listDocuments(kbId));
    }

    @DeleteMapping("/{kbId}/documents/{docId}")
    public Result<Void> deleteDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        manageService.deleteDocument(docId);
        return Result.success();
    }
}
