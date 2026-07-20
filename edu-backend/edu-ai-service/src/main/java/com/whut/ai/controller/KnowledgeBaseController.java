package com.whut.ai.controller;

import com.whut.ai.dto.DocumentChunk;
import com.whut.ai.dto.KnowledgeBaseBuildRequest;
import com.whut.ai.entity.KbKnowledgeBase;
import com.whut.ai.mapper.KbKnowledgeBaseMapper;
import com.whut.ai.rag.KnowledgeBaseService;
import com.whut.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RestController
@RequestMapping("/api/ai/knowledge")
public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseService knowledgeBaseService;
    private final KbKnowledgeBaseMapper kbMapper;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService,
                                    KbKnowledgeBaseMapper kbMapper) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.kbMapper = kbMapper;
    }

    @PostMapping("/build")
    public Result<Map<String, Object>> build(@RequestBody KnowledgeBaseBuildRequest request) {
        log.info("收到知识库构建请求 | kbId={} | 路径: {}", request.getKbId(), request.getDocumentPath());

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

    @PostMapping("/demo")
    public Result<Map<String, Object>> demo() {
        log.info("收到一键演示请求");

        try {
            // 从 classpath 提取 sample-docs 到临时目录，兼容 JAR 内运行
            Path tempDir = Files.createTempDirectory("rag-demo-");
            extractSampleDocs(tempDir);
            tempDir.toFile().deleteOnExit();

            log.info("示例文档已提取到: {}", tempDir);

            // 创建临时知识库
            KbKnowledgeBase kb = new KbKnowledgeBase();
            kb.setName("演示知识库");
            kb.setDescription("用于演示的知识库");
            kb.setCollectionName("demo_" + System.currentTimeMillis());
            kb.setStatus(1);
            kbMapper.insert(kb);
            log.info("临时知识库已创建 | kbId={}", kb.getId());

            // 构建知识库
            KnowledgeBaseBuildRequest buildRequest = new KnowledgeBaseBuildRequest();
            buildRequest.setKbId(kb.getId());
            buildRequest.setDocumentPath(tempDir.toString());
            buildRequest.setChunkSize(500);
            buildRequest.setChunkOverlap(50);

            KnowledgeBaseService.BuildResult buildResult = knowledgeBaseService.buildKnowledgeBase(buildRequest);

            // 执行检索
            String sampleQuery = "平台有哪些功能？";
            List<DocumentChunk> searchResults = knowledgeBaseService.searchSimilar(sampleQuery, 3);

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
            return Result.fail(500, "演示失败: " + e.getMessage());
        }
    }

    private void extractSampleDocs(Path tempDir) throws IOException {
        URL resourceUrl = getClass().getClassLoader().getResource("sample-docs");
        if (resourceUrl == null) {
            throw new IOException("未找到 sample-docs 资源目录");
        }

        String protocol = resourceUrl.getProtocol();
        if ("jar".equals(protocol)) {
            String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
            try (JarFile jarFile = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("sample-docs/") && !entry.isDirectory()) {
                        String fileName = entry.getName().substring("sample-docs/".length());
                        Path targetFile = tempDir.resolve(fileName);
                        try (InputStream in = jarFile.getInputStream(entry)) {
                            Files.copy(in, targetFile);
                        }
                    }
                }
            }
        } else {
            try {
                File sourceDir = new File(resourceUrl.toURI());
                File[] files = sourceDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Files.copy(file.toPath(), tempDir.resolve(file.getName()));
                    }
                }
            } catch (URISyntaxException e) {
                throw new IOException("解析资源路径失败", e);
            }
        }
    }
}
