package com.whut.ai.service;

import com.whut.ai.dto.ChatRequest;
import com.whut.ai.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT = """
            你是一个在线教育平台的智能助手。请根据下方提供的参考资料回答用户的问题。
            
            规则：
            1. 仅基于提供的参考资料回答问题，不要编造信息。
            2. 如果参考资料中没有相关信息，请诚实告知用户"当前知识库中暂无相关资料，建议联系老师获取更多帮助"。
            3. 回答要简洁、准确、友好。
            4. 适当引用资料来源。
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public ChatResponse chat(ChatRequest request) {
        log.info("收到 RAG 问答请求 | 问题: {}", request.getQuestion());

        String question = request.getQuestion();
        if (question == null || question.isBlank()) {
            return ChatResponse.fail("问题不能为空");
        }

        int topK = request.getTopK() > 0 ? request.getTopK() : 4;

        log.info("步骤1：语义检索知识库...");
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(topK).build());

        if (docs.isEmpty()) {
            log.info("知识库中未检索到相关内容");
            String fallback = chatClient.prompt()
                    .system("你是一个在线教育平台的智能助手。用户的问题在知识库中未找到相关内容，请礼貌地告知用户并建议他们联系老师。")
                    .user(question)
                    .call()
                    .content();
            return ChatResponse.ok(fallback, new ArrayList<>(), "deepseek-v4-pro");
        }

        log.info("检索到 {} 个相关文档块", docs.size());

        String context = docs.stream()
                .map(doc -> {
                    String source = (String) doc.getMetadata().getOrDefault("source", "未知来源");
                    return "【" + source + "】\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n---\n\n"));

        List<ChatResponse.Source> sources = docs.stream()
                .map(doc -> new ChatResponse.Source(
                        doc.getText(),
                        (String) doc.getMetadata().getOrDefault("source", "未知来源"),
                        doc.getMetadata().containsKey("distance")
                                ? 1.0 - ((Number) doc.getMetadata().get("distance")).doubleValue()
                                : 0.0))
                .collect(Collectors.toList());

        log.info("步骤2：组装 Prompt 并调用 DeepSeek...");
        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("参考资料如下：\n\n" + context + "\n\n---\n\n用户问题：" + question)
                .call()
                .content();

        log.info("RAG 问答完成 | 模型: deepseek-v4-pro | 检索到 {} 条来源", sources.size());
        return ChatResponse.ok(answer, sources, "deepseek-v4-pro");
    }
}
