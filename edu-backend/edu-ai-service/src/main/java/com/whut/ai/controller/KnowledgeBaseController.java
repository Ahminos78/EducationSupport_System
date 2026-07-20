package com.whut.ai.controller;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import com.whut.ai.rag.KnowledgeBaseService;
import com.whut.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * 提供知识库构建和检索的 REST API 接口。
 *
 * 接口列表
 * - POST /api/ai/knowledge/build - 构建知识库
 * - GET  /api/ai/knowledge/search - 相似性检索
 * - POST /api/ai/knowledge/demo - 一键演示（使用内置示例文档）
 *
 * 什么是 RAG？
 * RAG（Retrieval-Augmented Generation，检索增强生成）是一种让大模型
 * 结合外部知识库来回答问题的技术。它的工作流程是：
 *
 * 用户问题 → 检索知识库 → 找到相关内容 → 拼接到 Prompt 中 → 大模型生成回答
 *
 * 本 Controller 提供的就是"检索知识库"这一步的接口。
 */
@RestController
@RequestMapping("/api/ai/knowledge")
public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 构建知识库接口
     * 解析指定的文档或目录，向量化后存入 Chroma 向量数据库。
     *
     * 请求示例：
     * POST /api/ai/knowledge/build
     * Content-Type: application/json
     *
     * {
     *   "documentPath": "D:/docs/company_intro.pdf",
     *   "chunkSize": 500,
     *   "chunkOverlap": 50
     * }
     *
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "success": true,
     *     "totalChunks": 42,
     *     "totalDocuments": 3,
     *     "sourcePath": "D:/docs/",
     *     "message": "知识库构建成功！"
     *   }
     * }
     *
     * @param request 构建请求参数
     * @return 构建结果
     */
    @PostMapping("/build")
    public Result<Map<String, Object>> build(@RequestBody KnowledgeBaseBuildRequest request) {
        log.info("收到知识库构建请求 | 路径: {}", request.getDocumentPath());

        try {
            KnowledgeBaseService.BuildResult result = knowledgeBaseService.buildKnowledgeBase(request);

            return Result.success(Map.of(
                    "success", true,
                    "totalChunks", result.totalChunks(),
                    "totalDocuments", result.totalDocuments(),
                    "sourcePath", result.sourcePath(),
                    "message", "知识库构建成功！"
            ));
        } catch (Exception e) {
            log.error("知识库构建失败", e);
            return Result.fail(500, "知识库构建失败: " + e.getMessage());
        }
    }

    /**
     * 相似性检索接口
     * 根据用户的问题，在知识库中找到最相关的文本块。
     * 这是 RAG 系统中"检索"那一步的核心接口。
     *
     * 请求示例：
     * GET /api/ai/knowledge/search?query=课程的主要内容是什么&topK=5
     *
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "success": true,
     *     "total": 3,
     *     "results": [
     *       {
     *         "id": "abc123",
     *         "content": "平台的主要功能包括...",
     *         "sourceDocument": "课程介绍.pdf",
     *         "chunkIndex": 2,
     *         "metadata": {...}
     *       }
     *     ]
     *   }
     * }
     *
     * @param query 用户的查询问题
     * @param topK  返回最相似的前 K 个结果（默认 4）
     * @return 相似文档块列表
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "4") int topK) {
        log.info("收到检索请求 | 查询: {} | TopK: {}", query, topK);

        try {
            List<DocumentChunk> results = knowledgeBaseService.searchSimilar(query, topK);

            return Result.success(Map.of(
                    "success", true,
                    "total", results.size(),
                    "results", results
            ));
        } catch (Exception e) {
            log.error("检索失败", e);
            return Result.fail(500, "检索失败: " + e.getMessage());
        }
    }

    /**
     * 一键演示接口
     * 使用内置的示例文档快速演示知识库构建和检索功能。
     * 方便快速验证整个流程是否正常工作。
     *
     * 演示内容
     * 1. 读取 classpath 下的示例文档
     * 2. 解析并分块
     * 3. 向量化并存入 Chroma
     * 4. 执行一个示例检索
     * 5. 返回完整的演示结果
     *
     * @return 演示结果
     */
    @PostMapping("/demo")
    public Result<Map<String, Object>> demo() {
        log.info("收到一键演示请求");

        try {
            // 从 classpath 获取示例文档路径
            String demoDocPath = getClass().getClassLoader().getResource("sample-docs") != null
                    ? getClass().getClassLoader().getResource("sample-docs").getPath()
                    : null;

            if (demoDocPath == null) {
                return Result.fail(400, "未找到示例文档目录 sample-docs，请确保资源文件存在");
            }

            // Windows 下路径可能以 / 开头，需要处理
            if (demoDocPath.startsWith("/") && demoDocPath.contains(":")) {
                demoDocPath = demoDocPath.substring(1);
            }
            // URL 解码（处理路径中的空格和中文）
            demoDocPath = java.net.URLDecoder.decode(demoDocPath, "UTF-8");

            log.info("示例文档路径: {}", demoDocPath);

            // ========== 第一步：构建知识库 ==========
            KnowledgeBaseBuildRequest buildRequest = new KnowledgeBaseBuildRequest();
            buildRequest.setDocumentPath(demoDocPath);
            buildRequest.setChunkSize(500);
            buildRequest.setChunkOverlap(50);

            KnowledgeBaseService.BuildResult buildResult =
                    knowledgeBaseService.buildKnowledgeBase(buildRequest);

            // ========== 第二步：执行示例检索 ==========
            String sampleQuery = "平台有哪些功能？";
            List<DocumentChunk> searchResults =
                    knowledgeBaseService.searchSimilar(sampleQuery, 3);

            log.info("演示完成 | 构建 {} 个块 | 检索返回 {} 条结果",
                    buildResult.totalChunks(), searchResults.size());

            return Result.success(Map.of(
                    "success", true,
                    "message", "知识库构建与检索演示成功！",
                    "buildResult", Map.of(
                            "totalChunks", buildResult.totalChunks(),
                            "totalDocuments", buildResult.totalDocuments(),
                            "sourcePath", buildResult.sourcePath()
                    ),
                    "searchDemo", Map.of(
                            "query", sampleQuery,
                            "resultsCount", searchResults.size(),
                            "topResult", searchResults.isEmpty() ? null : searchResults.get(0).getContent()
                    )
            ));

        } catch (Exception e) {
            log.error("演示失败", e);
            return Result.fail(500, "演示失败: " + e.getMessage()
                    + "。请确保 Chroma 服务已启动，并且配置正确");
        }
    }
}
