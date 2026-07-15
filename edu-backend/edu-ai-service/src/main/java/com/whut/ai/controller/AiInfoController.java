package com.whut.ai.controller;

import com.whut.ai.config.AiProperties;
import com.whut.ai.config.DeepSeekProperties;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 服务信息与健康检查接口。
 */
@RestController
@RequestMapping("/api/ai")
public class AiInfoController {

    private final DeepSeekProperties deepSeekProperties;
    private final AiProperties aiProperties;

    public AiInfoController(DeepSeekProperties deepSeekProperties, AiProperties aiProperties) {
        this.deepSeekProperties = deepSeekProperties;
        this.aiProperties = aiProperties;
    }

    /**
     * 服务健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        boolean hasApiKey = deepSeekProperties.getApiKey() != null
                && !deepSeekProperties.getApiKey().isBlank();
        return Result.success(Map.of(
                "status", "UP",
                "model", deepSeekProperties.getModel(),
                "configured", hasApiKey,
                "ragEnabled", aiProperties.getRag().isEnabled()
        ));
    }

    /**
     * AI 服务配置信息（不暴露 API Key）
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(Map.of(
                "defaultModel", aiProperties.getDefaultModel(),
                "deepseekModel", deepSeekProperties.getModel(),
                "deepseekBaseUrl", deepSeekProperties.getBaseUrl(),
                "ragEnabled", aiProperties.getRag().isEnabled(),
                "vectorStoreMaxResults", aiProperties.getVectorStore().getMaxResults(),
                "similarityThreshold", aiProperties.getVectorStore().getSimilarityThreshold()
        ));
    }
}
